package lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JdtLsGotoDefinition {

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
                "-jar", "plugins/org.eclipse.equinox.launcher_1.7.100.v20251111-0406.jar",
                "-configuration", "config_linux",
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
        init.setRootUri("file://" +
                this.workspace);

        // Minimale Client-Capabilities (sonst sendet JDT-LS nichts!)
        ClientCapabilities caps = new ClientCapabilities();
        TextDocumentClientCapabilities textCaps = new TextDocumentClientCapabilities();
        textCaps.setDefinition(new DefinitionCapabilities());
        caps.setTextDocument(textCaps);
        init.setCapabilities(caps);

        server.initialize(init).get();

    }

    public void shutdownServer() throws ExecutionException, InterruptedException {
        server.shutdown().get();
        server.exit();
    }

    public void openFile(String fileName) throws InterruptedException, IOException {
        String filePath = this.workspace +
                fileName;
        String fileUri = "file://" + filePath;

        String content = Files.readString(Paths.get(filePath));

        TextDocumentItem item =
                new TextDocumentItem(fileUri, "java", 1, content);

        server.getTextDocumentService().didOpen(
                new DidOpenTextDocumentParams(item)
        );
    }

    public GoTo findDefinition(int row, int col, String fileName) throws ExecutionException, InterruptedException {
        String filePath = this.workspace +
                fileName;
        String uriPrefix = "file://";
        String fileUri = uriPrefix + filePath;

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

                return new GoTo(loc.getUri().substring(uriPrefix.length()), loc.getRange().getStart().getLine(), loc.getRange().getStart().getCharacter());
            }
        }

        if (result.isRight()) {
            List<? extends LocationLink> links = result.getRight();
            for (LocationLink link : links) {
                return new GoTo(link.getTargetUri().substring(uriPrefix.length()), link.getTargetRange().getStart().getLine(), link.getTargetRange().getStart().getCharacter());
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

        JdtLsGotoDefinition jdtLsGotoDefinition = new JdtLsGotoDefinition();
        jdtLsGotoDefinition.setWorkspace( "/home/oem/IdeaProjects/mein");
        jdtLsGotoDefinition.startServer();
        Thread.sleep(4000);

        jdtLsGotoDefinition.openFile("/src/main/java/Tester.java");
        Thread.sleep(2000);


        jdtLsGotoDefinition.findDefinition(2, 27, "/src/main/java/Tester.java");

        jdtLsGotoDefinition.shutdownServer();
    }


    public String getWorkspace() {
        return this.workspace;
    }
}
