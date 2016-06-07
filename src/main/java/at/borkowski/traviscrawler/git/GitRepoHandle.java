package at.borkowski.traviscrawler.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class GitRepoHandle {
    private final boolean deleteOnFinalize;
    private final Git git;
    private boolean deleted = false;

    public GitRepoHandle(Git git, boolean deleteOnFinalize) {
        this.git = git;
        this.deleteOnFinalize = deleteOnFinalize;
    }

    public void checkout(String commit) throws GitAPIException {
        git.checkout().setName(commit).call();
    }

    public void delete() {
        removeRecursive(Paths.get(git.getRepository().getWorkTree().getAbsolutePath()));
        removeRecursive(Paths.get(git.getRepository().getWorkTree().getAbsolutePath()));
    }

    public File getWorkTree() {
        return git.getRepository().getWorkTree();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (deleteOnFinalize && !deleted) delete();
        } catch (Throwable ignored) {
        }
        super.finalize();
    }

    // stolen from http://stackoverflow.com/a/8685959
    private static void removeRecursive(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // try to delete the file anyway, even if its attributes
                    // could not be read, since delete-only access is
                    // theoretically possible
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed; propagate exception
                        throw exc;
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
