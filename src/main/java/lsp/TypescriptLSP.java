package lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TypescriptLSP implements LSP{
    private String workspace;

    private  LanguageServer server;


    @Override
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
    @Override
    public void startServer() throws IOException, ExecutionException, InterruptedException {
    Process process = new ProcessBuilder(
            "npx",
            "--yes",
            "typescript-language-server",
            "--stdio"
    ).start();

    InputStream in = process.getInputStream();
    OutputStream out = process.getOutputStream();


    Launcher<LanguageServer> launcher =
            LSPLauncher.createClientLauncher(new LanguageClientAdapter(), in, out);

    launcher.startListening();
    this.server = launcher.getRemoteProxy();

    initialize(server, this.workspace);
}


    private void initialize(LanguageServer server, String projectPath) throws ExecutionException, InterruptedException {
        String uri = Path.of(projectPath).toUri().toString();

        InitializeParams params = new InitializeParams();
        params.setRootUri(uri);

        WorkspaceFolder folder = new WorkspaceFolder(uri, "workspace");
        params.setWorkspaceFolders(List.of(folder));

        ClientCapabilities caps = new ClientCapabilities();
        WorkspaceClientCapabilities wc = new WorkspaceClientCapabilities();
        wc.setWorkspaceFolders(true);
        caps.setWorkspace(wc);

        params.setCapabilities(caps);

        server.initialize(params).get();
    }

    @Override
    public void openFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);

        String fileUri = path.toUri().toString();

        String content = Files.readString(path);

        TextDocumentItem item =
                new TextDocumentItem(fileUri, "typescriptreact", 1, content);

        server.getTextDocumentService().didOpen(
                new DidOpenTextDocumentParams(item)
        );
    }

    @Override
    public GoTo findDefinition(int row, int col, String fileName) throws ExecutionException, InterruptedException {
        return JdtLsGotoDefinition.findDef(this.server, row, col, fileName);
    }

    @Override
    public String getWorkspace() {
        return this.workspace;
    }

    @Override
    public void shutdownServer() throws ExecutionException, InterruptedException {

    }
}
