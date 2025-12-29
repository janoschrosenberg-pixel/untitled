package lsp;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface LSP {
    void startServer() throws IOException, ExecutionException, InterruptedException;
    void setWorkspace(String workspace);
    void openFile(String fileName) throws InterruptedException, IOException;
    GoTo findDefinition(int row, int col, String fileName) throws ExecutionException, InterruptedException;
    String getWorkspace();
    void shutdownServer() throws ExecutionException, InterruptedException;
}
