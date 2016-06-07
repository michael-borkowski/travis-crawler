package at.borkowski.traviscrawler.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitRepo {
    public static GitRepoHandle grab(String slug) throws GitAPIException, IOException {
        String uri = "git@github.com:" + slug + ".git";
        Path localDirectory = Files.createTempDirectory("github-crawl-");
        Git git = Git.cloneRepository()
                .setCloneAllBranches(false).setCloneSubmodules(true)
                .setDirectory(localDirectory.toFile()).setNoCheckout(true).setURI(uri).call();

        git.fetch().call();

        return new GitRepoHandle(git, true);
    }
}
