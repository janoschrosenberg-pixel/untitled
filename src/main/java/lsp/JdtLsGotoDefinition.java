package lsp;

import editor.Utils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;

import org.eclipse.lsp4j.services.LanguageServer;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class JdtLsGotoDefinition  implements LSP{

    private String workspace;

    private  LanguageServer server;

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void startServer() throws IOException, ExecutionException, InterruptedException {
        Process process = new ProcessBuilder(
                "java",
                "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                "-Dosgi.bundles.defaultStartLevel=4",
                "-Declipse.product=org.eclipse.jdt.ls.core.product",
                "-Dlog.level=ALL",
                "-noverify",
                "-jar", "org.eclipse.equinox.launcher_1.7.100.v20251111-0406.jar",
                "-configuration", Utils.getOsConfig(),
                "-data", this.workspace
        )
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();


        LanguageClientAdapter client = new LanguageClientAdapter();

        Launcher<LanguageServer> launcher =
                LSPLauncher.createClientLauncher(client, in, out);

        this.server = launcher.getRemoteProxy();
        launcher.startListening();

        // ------------------------------------------------------------
        // 3. Initialisierung
        // ------------------------------------------------------------
        InitializeParams init = new InitializeParams();
        init.setProcessId((int) ProcessHandle.current().pid());

        init.setRootUri(Paths.get( this.workspace).toUri().toString());

        // Minimale Client-Capabilities (sonst sendet JDT-LS nichts!)
        ClientCapabilities caps = new ClientCapabilities();
        TextDocumentClientCapabilities textCaps = new TextDocumentClientCapabilities();
        textCaps.setDefinition(new DefinitionCapabilities());
        caps.setTextDocument(textCaps);
        init.setCapabilities(caps);

        server.initialize(init).get();
        server.initialized(new InitializedParams());
    }

    public void shutdownServer() throws ExecutionException, InterruptedException {
        server.shutdown().get();
        server.exit();
    }

    public void openFile(String fileName) throws  IOException {

        Path path = Paths.get(fileName);

        String fileUri = path.toUri().toString();

        String content = Files.readString(path);

        TextDocumentItem item =
                new TextDocumentItem(fileUri, "java", 1, content);

        server.getTextDocumentService().didOpen(
                new DidOpenTextDocumentParams(item)
        );
    }

    public GoTo findDefinition(int row, int col, String fileName) throws ExecutionException, InterruptedException {
        return findDef(server, row, col, fileName);
    }

    public static GoTo findDef(LanguageServer server, int row, int col, String fileName) throws InterruptedException, ExecutionException {
        Path path = Paths.get(fileName);
        String fileUri = path.toUri().toString();

        DefinitionParams dp = new DefinitionParams(
                new TextDocumentIdentifier(fileUri),
                new Position(row, col)
        );

        CompletableFuture<
                Either<List<? extends Location>, List<? extends LocationLink>>
                > future = server.getTextDocumentService().definition(dp);

        Either<List<? extends Location>, List<? extends LocationLink>> result = future.get();

        // ------------------------------------------------------------
        // 6. Ergebnis verarbeiten
        // ------------------------------------------------------------
        if (result.isLeft()) {
            List<? extends Location> locs = result.getLeft();
            for (Location loc : locs) {
                Path currentPath = Paths.get(URI.create(loc.getUri()));
                return new GoTo(currentPath.toString(), loc.getRange().getStart().getLine(), loc.getRange().getStart().getCharacter());
            }
        }

        if (result.isRight()) {
            List<? extends LocationLink> links = result.getRight();
            for (LocationLink link : links) {
                Path currentPath = Paths.get(URI.create(link.getTargetUri()));
                return new GoTo(currentPath.toString(), link.getTargetRange().getStart().getLine(), link.getTargetRange().getStart().getCharacter());
            }
        }
        return null;
    }




    public String getWorkspace() {
        return this.workspace;
    }
}
