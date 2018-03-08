package gitlet;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.text.Format;


/**
 * Created by Sara on 12/3/17.
 *
 * @author Sara Wang
 */
public class CommitTree implements Serializable {

    /**
     * Hashmap of all the branches in the commit tree.
     */
    private HashMap<String, Branch> branches;

    /**
     * Hashmap of all the commits made.
     */
    private HashMap<String, Commit> commits;

    /**
     * Hashmap of commit messages in correspondance to commmit ids.
     */
    private HashMap<String, ArrayList<String>> messages;

    /**
     * Represent the current branch.
     */
    private Branch currentBranch;

    /**
     * Represent the head commit.
     */
    private Commit headCommit;

    /**
     * Represent the staging area.
     */
    private HashMap<String, Blob> staging;

    /**
     * Represent the remove area, where files are to be untracked.
     */
    private HashMap<String, Blob> remove;

    /**
     * Represent all the split points.
     */
    private HashMap<Commit, ArrayList<Branch>> splitPoint;

    /**
     * Represent the existent remotes, name, path.
     */
    private HashMap<String, String> remote;

    /**
     * A commit tree of all information.
     */
    public CommitTree() {
        this.branches = new HashMap<String, Branch>();
        this.commits = new HashMap<String, Commit>();
        this.messages = new HashMap<String, ArrayList<String>>();
        this.staging = new HashMap<String, Blob>();
        this.remove = new HashMap<String, Blob>();
        this.splitPoint = new HashMap<Commit, ArrayList<Branch>>();
        this.remote = new HashMap<String, String>();

    }


    /**
     * Check if there is already a remote named this.
     * @return whether the remote exist
     * @param remoteName name of remote
     */
    public boolean remoteExist(String remoteName) throws IOException {
        boolean b = remote.containsKey(remoteName);
        return b;
    }


    /**
     * Rm remote command.
     * @param remoteName name of remote to be rm
     */
    public void rmRemote(String remoteName) throws IOException {
        if (!remoteExist(remoteName)) {
            System.err.println(" A remote with that name does not exist.");
            System.exit(0);
        }

        remote.remove(remoteName);
    }


    /**
     * Push command.
     * @param remoteBranchName the branch to push to
     * @param remoteName name of the remote to push to
     */
    public void push(String remoteName, String
            remoteBranchName) throws IOException {
        String remotePath = remote.get(remoteName);
        File remoteRepo = new File(remotePath);
        if (!remoteRepo.exists()) {
            System.err.println("Remote directory not found.");
            System.exit(0);
        }
    }



    /**
     * AddRemote command.
     * @param remoteName name of remote to add
     * @param remotePath path of the remote to add
     */
    public void addRemote(String remoteName,
                          String remotePath) throws IOException {
        String path = remotePath.replaceAll("\b/",
                java.io.File.separator);
        remote.put(remoteName, path);
        if (remoteExist(remoteName)) {
            System.out.println(
                    " A remote with that name already exists.");
            System.exit(0);
        }
    }

    /**
     * Init command.
     */
    public void commitInit() throws IOException {
        File gitlet = new File(".gitlet");
        if (gitlet.isDirectory()) {
            System.out.println("A gitlet version-control "
                    + "system already exists in the current directory.");
            System.exit(0);
        }
        gitlet.mkdir();

        File commit = new File(".gitlet" + File.separator + "commit");
        commit.mkdir();


        Commit init = new Commit();
        Branch master = new Branch("master", null);
        master.add(init);
        currentBranch = master;
        headCommit = init;

        ArrayList<String> commitsWithThisMessage = new ArrayList<>();
        commitsWithThisMessage.add(init.getId());

        this.branches.put("master", master);
        this.commits.put(init.getId(), init);
        this.messages.put(init.getCommitMessage(), commitsWithThisMessage);

    }

    /**
     * Add command.
     *
     * @param filename the file to add.
     */
    public void add(String filename) throws IOException {
        File addFile = new File(filename);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob toAdd = new Blob(filename);
        if (headCommit.getBlobs() != null
                && headCommit.getBlobs().containsKey(filename)) {
            Blob b = headCommit.getBlobs().get(filename);
            if (new String(b.getContent(),
                    StandardCharsets.UTF_8).equals(new String(
                            toAdd.getContent(), StandardCharsets.UTF_8))) {
                if (staging.containsKey(filename)) {
                    staging.remove(filename);
                }
                if (remove.containsKey(filename)) {
                    remove.remove(filename);
                }
            } else {
                staging.put(filename, toAdd);
            }
        } else {
            if (staging.containsKey(filename)) {
                staging.replace(filename, toAdd);
            }
            if (remove.containsKey(filename)) {
                remove.remove(filename);
                staging.put(filename, toAdd);
            } else {
                staging.put(filename, toAdd);
            }
        }
    }

    /**
     * Commit command.
     * @param commitMessage the commit message of the commit to be made
     * @param anotherParent another parent
     */
    public void commit(String commitMessage, Commit anotherParent)
            throws IOException {
        if (commitMessage.equals("")) {
            System.out.println(" Please enter a commit message.");
            System.exit(0);
        }
        if (staging.isEmpty() && remove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        HashMap<String, Blob> blobs = new HashMap<>();

        blobs.putAll(headCommit.getBlobs());

        for (String removeFilename : remove.keySet()) {
            blobs.remove(removeFilename);
        }

        for (String addFilename : staging.keySet()) {
            if (blobs.containsKey(addFilename)) {
                blobs.remove(addFilename);
            }
            blobs.put(addFilename, staging.get(addFilename));
        }

        ArrayList<Commit> parents = new ArrayList<>();

        parents.add(headCommit);
        if (anotherParent != null) {
            parents.add(anotherParent);
        }
        Date d = new Date();
        Commit c = new Commit(commitMessage, new Date(),
                blobs, parents);
        this.headCommit = c;
        currentBranch.add(c);
        commits.put(c.getId(), c);
        currentBranch.changeHead(c);
        if (messages.containsKey(c.getCommitMessage())) {
            messages.get(c.getCommitMessage()).add(c.getId());
        } else {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(c.getId());
            messages.put(c.getCommitMessage(), temp);
        }
        staging.clear();
        remove.clear();


    }

    /**
     * Remove command.
     *
     * @param filename the file to be removed.
     */
    public void rm(String filename) {

        if (!staging.containsKey(filename)
                && !headCommit.getBlobs().containsKey(filename)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        File f = new File(filename);
        Blob removeBlob;
        if (!f.exists()) {
            removeBlob = new Blob(filename, false);
        } else {
            removeBlob = new Blob(filename);
        }

        if (staging.containsKey(filename)) {
            staging.remove(filename);
        }

        if (headCommit.getBlobs().containsKey(filename)) {
            remove.put(filename, removeBlob);
            File removedFile = new File(filename);
            if (removedFile.exists()) {
                removedFile.delete();
            }
        }


    }

    /**
     * Log command, to print out all previous commits.
     */
    public void log() {


        ArrayList<Commit> commitsOnCurrentBranch
                = currentBranch.getCommits();

        Boolean print = false;

        for (int i = commitsOnCurrentBranch.size() - 1; i >= 0; i--) {
            Commit temp = commitsOnCurrentBranch.get(i);
            if (temp.getId().equals(headCommit.getId())) {
                print = true;
            }
            if (!print) {
                continue;
            }

            System.out.println("===");
            System.out.println("commit " + temp.getId());

            if (temp.getParents() != null && temp.getParents().size() > 1) {
                ArrayList<Commit> tempParents = temp.getParents();
                System.out.println("Merge: "
                        + tempParents.get(0).getId().substring(0, 7) + " "
                        + tempParents.get(1).getId().substring(0, 7));
            }

            Date time = temp.getCommitDate();
            Format format = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            String printDate = format.format(time);
            System.out.println("Date: " + printDate);

            System.out.println(temp.getCommitMessage());
            if (i > 0) {
                System.out.println();
            }
        }
    }

    /**
     * Global-log command, to print out all commits made.
     */
    public void globalLog() {
        int count = 0;

        for (Commit c : commits.values()) {

            System.out.println("===");
            System.out.println("commit " + c.getId());

            if (c.getParents() != null && c.getParents().size() > 1) {
                ArrayList<Commit> tempParents = c.getParents();
                System.out.println("Merge: "
                        + tempParents.get(0).getId().substring(0, 8)
                        + tempParents.get(1).getId().substring(0, 8));
            }

            Date time = c.getCommitDate();
            Format format = new
                    SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            String printDate = format.format(time);
            System.out.println("Date: " + printDate);
            System.out.println(c.getCommitMessage());
            if (count < commits.size() - 1) {
                System.out.println();
            }
            count++;
        }
    }

    /**
     * Find command.
     *
     * @param commitMessage the commit message to look for.
     */
    public void find(String commitMessage) {
        if (!messages.containsKey(commitMessage)) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        } else {
            ArrayList<String> print = messages.get(commitMessage);
            for (int i = 0; i < print.size(); i++) {
                System.out.println(print.get(i));
            }
        }
    }

    /**
     * Status command.
     */
    public void status() {
        System.out.println("=== Branches ===");

        ArrayList<String> printBranches = new ArrayList<>();
        for (String e : branches.keySet()) {
            printBranches.add(e);
        }
        Collections.sort(printBranches);
        for (String p : printBranches) {
            if (p.equals(currentBranch.getName())) {
                System.out.print("*");
            }
            System.out.println(p);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");

        ArrayList<String> stagedFiles = new ArrayList<>();
        for (String e : staging.keySet()) {
            stagedFiles.add(e);
        }
        Collections.sort(stagedFiles);
        for (String p : stagedFiles) {
            System.out.println(p);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");

        ArrayList<String> removedFiles = new ArrayList<>();
        for (String e : remove.keySet()) {
            removedFiles.add(e);
        }
        Collections.sort(removedFiles);
        for (String p : removedFiles) {
            System.out.println(p);
        }
        System.out.println();

        System.out.println(
                "=== Modifications Not Staged For Commit ===");
        statusModified();

        System.out.println("=== Untracked Files ===");
        statusUntracked();
    }

    /**
     * For the modified files when calling status command.
     */
    public void statusModified() {
        ArrayList<String> modifiedFiles = new ArrayList<>();
        for (String filename: headCommit.getBlobs().keySet()) {
            File f = new File(filename);
            String trackedContent = new String(headCommit.getBlobs()
                    .get(filename).getContent(), StandardCharsets.UTF_8);
            if (f.exists()) {
                String workingContent = Utils.readContentsAsString(f);
                if (!trackedContent.equals(workingContent)) {
                    String temp = filename + " (modified)";
                    modifiedFiles.add(temp);
                }
            } else {
                if (!remove.keySet().contains(filename)) {
                    String temp = filename + " (deleted)";
                    modifiedFiles.add(temp);
                }
            }
        }
        for (String filename: staging.keySet()) {
            File f = new File(filename);
            String stagedFileContent =
                    new String(staging.get(filename).getContent(),
                            StandardCharsets.UTF_8);
            if (f.exists()) {
                String workingContent = Utils.readContentsAsString(f);
                if (!stagedFileContent.equals(workingContent)) {
                    if (!modifiedFiles.contains(filename)) {
                        String temp = filename + " (modified)";
                        modifiedFiles.add(temp);
                    }
                }
            } else {
                if (!modifiedFiles.contains(filename)) {
                    String temp = filename + " (deleted)";
                    modifiedFiles.add(temp);
                }
            }
        }
        if (modifiedFiles == null) {
            System.out.println();
            return;
        }
        Collections.sort(modifiedFiles);
        for (String o: modifiedFiles) {
            System.out.println(o);
        }

        System.out.println();

    }

    /**
     * For the untracked files when calling status command.
     */
    public void statusUntracked() {
        ArrayList<String> untrackedFiles = new ArrayList<>();
        HashMap<String, Blob> trackedFiles = headCommit.getBlobs();
        if (trackedFiles == null) {
            System.out.println();
            return;
        }

        for (String filename: Utils.plainFilenamesIn(".")) {
            if (!staging.containsKey(filename)
                    && !trackedFiles.containsKey(filename)) {
                untrackedFiles.add(filename);
            }
            if (trackedFiles.containsKey(filename)
                    && remove.containsKey(filename)) {
                untrackedFiles.add(filename);
            }
        }

        Collections.sort(untrackedFiles);
        for (String i: untrackedFiles) {
            System.out.println(i);
        }
    }


    /**
     * Checkout command 1, checkout one file from the head commit.
     *
     * @param args operand that commands the file to be checked out
     */
    public void checkOutFromHeadCommit(String... args)
            throws IOException {
        String operand = args[1];
        if (!operand.equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String filename = args[2];
        if (headCommit.getBlobs() == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        if (!headCommit.getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob b = headCommit.getBlobs().get(filename);
        File f = new File(filename);

        if (!f.exists()) {
            f.createNewFile();
        }

        Utils.writeContents(f, b.getContent());

    }

    /**
     * Checkout command 2, checkout one file from a specified commit.
     *
     * @param args operand that commands the file to be check out
     */
    public void checkOutFromSomeCommit(String... args)
            throws IOException {

        String operand = args[2];
        if (!operand.equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String filename = args[3];
        String commitId = args[1];
        for (String s: commits.keySet()) {
            if (s.substring(0, commitId.length()).equals(commitId)) {
                commitId = s;
                break;
            }
        }

        if (!commits.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }


        Commit c = commits.get(commitId);
        if (!c.getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob b = c.getBlobs().get(filename);
        File f = new File(filename);

        if (!f.exists()) {
            f.createNewFile();
        }
        Utils.writeContents(f, b.getContent());
    }

    /**
     * Checkout command 3, checkout all the files
     * from the latest commit of a given branch.
     *
     * @param branchname given branch
     */
    public void checkOutFromBranch(String branchname) throws IOException {
        if (!branches.containsKey(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (currentBranch.getName().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Branch br = branches.get(branchname);
        Commit co = br.getHead();
        HashMap<String, Blob> checkOutFiles = co.getBlobs();
        HashMap<String, Blob> trackedFiles = headCommit.getBlobs();

        File workDir = new File(".");
        for (String f : Utils.plainFilenamesIn(workDir)) {
            if (!trackedFiles.containsKey(f)
                    && checkOutFiles.containsKey(f)) {
                Blob b = checkOutFiles.get(f);
                Blob bb = new Blob(f);
                if (!new String(b.getContent(), StandardCharsets.UTF_8).equals(
                        new String(bb.getContent(), StandardCharsets.UTF_8))) {
                    System.out.println(
                            "There is an untracked file in "
                                    + "the way; delete it or add it first.");
                    System.exit(0);
                }
            }
        }
        for (String f : Utils.plainFilenamesIn(workDir)) {
            if (trackedFiles.containsKey(f) && !checkOutFiles.containsKey(f)) {
                File tf = new File(f);
                tf.delete();
            }
        }


        for (String f : checkOutFiles.keySet()) {
            File file = new File(f);
            if (!file.exists()) {
                file.createNewFile();
            }
            Blob b = checkOutFiles.get(f);
            Utils.writeContents(file, b.getContent());
        }

        headCommit = co;
        staging.clear();
        currentBranch = br;

    }

    /**
     * Branch command, add a new branch.
     *
     * @param branchname name of the new branch
     */
    public void branch(String branchname) {

        if (branches.containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch br = new Branch(branchname, headCommit);
        branches.put(branchname, br);

        ArrayList<Branch> b = new ArrayList<>();
        b.add(currentBranch);
        b.add(br);

        splitPoint.put(headCommit, b);

        br.addAll(currentBranch);

    }

    /**
     * Remove branch command, remove a branch.
     *
     * @param branchname name of the branch to remove
     */
    public void rmBranch(String branchname) {
        if (!branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branches.get(branchname).equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        Branch thisBranch = branches.get(branchname);
        for (Commit c : splitPoint.keySet()) {
            ArrayList<Branch> branchMap = splitPoint.get(c);
            if (branchMap.contains(thisBranch)) {
                splitPoint.remove(c);
            }
        }
        branches.remove(branchname);
    }

    /**
     * Check if two commits are on the same branch.
     * @param c1 commit 1
     * @param c2 commit 2
     * @return bool indicating if c1 and c2 are on the same branch
     */
    public boolean sameBranch(Commit c1, Commit c2) {
        for (Branch b: branches.values()) {
            if (b.getCommits().contains(c1)
                    && b.getCommits().contains(c2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reset command, checkout all files from a specified previous commit.
     *
     * @param commitId the specified commit
     */
    public void
        reset(String commitId)
            throws IOException {
        for (String s: commits.keySet()) {
            if (s.substring(0, commitId.length()).equals(commitId)) {
                commitId = s;
                break;
            }
        }
        if (!commits.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        HashMap<String, Blob> trackedFiles = headCommit.getBlobs();
        List<String> workdirFiles = Utils.plainFilenamesIn(".");

        Commit c = commits.get(commitId);
        HashMap<String, Blob> toCheckoutFiles = c.getBlobs();

        for (String f : workdirFiles) {
            if (!trackedFiles.containsKey(f)
                    && toCheckoutFiles.containsKey(f)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                System.exit(0);
            }
        }
        for (String f : trackedFiles.keySet()) {
            if (!toCheckoutFiles.containsKey(f)) {
                File tf = new File(f);
                rm(f);
            }
        }
        for (String f : toCheckoutFiles.keySet()) {
            File file = new File(f);
            if (!file.exists()) {
                file.createNewFile();
            }
            Blob b = toCheckoutFiles.get(f);
            Utils.writeContents(file, b.getContent());
        }
        headCommit = c;
        Branch master = branches.get("master");

        if (master.getCommits().contains(c)) {
            currentBranch = master;
        } else {
            for (Branch b: branches.values()) {
                if (b.getCommits().contains(c)) {
                    currentBranch = b;
                    break;
                }
            }
        }
        currentBranch.changeHead(headCommit);
        staging.clear();
        remove.clear();
    }

    /**
     * Check if c1 is a parent of c2.
     * @param c1 commit 1
     * @param c2 commit 2
     * @return bool indicating if c1 is a parent of c2
     */
    public boolean isParentOf(Commit c1, Commit c2) {
        Branch current = currentBranch;
        for (String br: branches.keySet()) {
            if (branches.get(br).getCommits().contains(c1)
                    && branches.get(br).getCommits().contains(c2)) {
                current = branches.get(br);
                if (current.getCommits().indexOf(c1)
                        < current.getCommits().indexOf(c2)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Preprocess of merge.
     * @param givenBranchName the name of the branch to merge with
     */
    public void mergePre(String givenBranchName) {
        if (!staging.isEmpty() || !remove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(givenBranchName)) {
            System.out.println("A branch with "
                    + "that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.getName().equals(givenBranchName)) {
            System.out.println("Cannot "
                    + "merge a branch with itself.");
            System.exit(0);
        }
    }
    /**
     * Merge command.
     *
     * @param givenBranchName the name of the branch to merge with
     */
    public void merge(String givenBranchName) throws IOException {

        mergePre(givenBranchName);
        Branch givenBranch = branches.get(givenBranchName);
        Commit givenBranchHeadCommit =
                givenBranch.getCommits().get(
                givenBranch.getCommits().size() - 1);
        Commit splitPointCommit =
                givenBranchHeadCommit;
        for (Commit temp : splitPoint.keySet()) {
            ArrayList<Branch> b = splitPoint.get(temp);
            if (b.contains(currentBranch) && b.contains(givenBranch)) {
                splitPointCommit = temp;
                break;
            }
        }

        mergeHelper1(givenBranchHeadCommit);

        if (isParentOf(givenBranchHeadCommit, headCommit)) {
            System.out.println(
                    "Given branch is an ancestor "
                            + "of the current branch.");
            return;
        } else if (isParentOf(headCommit, givenBranchHeadCommit)) {
            System.out.println("Current branch fast-forwarded.");
            checkOutFromBranch(givenBranchName);
            return;
        } else {
            String commitMessage = "Merged "
                    + givenBranchName + " into "
                    + currentBranch.getName() + ".";
            HashMap<String, Blob> currentBranchHeadCommitFiles
                    = headCommit.getBlobs();
            HashMap<String, Blob> givenBranchHeadCommitFiles
                    = givenBranchHeadCommit.getBlobs();
            ArrayList<String> allFiles = new ArrayList<String>();
            allFiles.addAll(
                    currentBranchHeadCommitFiles.keySet());
            for (String temp
                    : givenBranchHeadCommitFiles.keySet()) {
                if (!allFiles.contains(temp)) {
                    allFiles.add(temp);
                }
            }
            for (String blobName : allFiles) {
                boolean s =
                        blobInThisCommit(splitPointCommit, blobName);
                boolean g =
                        blobInThisCommit(givenBranchHeadCommit, blobName);
                boolean c =
                        blobInThisCommit(headCommit, blobName);
                mergeHelper2(currentBranchHeadCommitFiles, blobName,
                        splitPointCommit, givenBranchHeadCommitFiles,
                        givenBranchHeadCommit, s, c, g);
            }
            commit(commitMessage, givenBranchHeadCommit);
        }
    }

    /**
     * Merge helper 1.
     * @param givenBranchHeadCommit Head commit of the given branch
     */
    public void mergeHelper1(Commit givenBranchHeadCommit) {
        for (String filenames: Utils.plainFilenamesIn(".")) {
            if (givenBranchHeadCommit.getBlobs().containsKey(filenames)
                    && !headCommit.getBlobs().containsKey(filenames)) {
                System.out.println(
                        "There is an untracked file in the way; "
                                + "delete it or add it first.");
                System.exit(0);
            }
        }
    }

    /**
     * Merge helper 2.
     * @param currentBranchHeadCommitFiles files in the current
     *                                     branch's head commit
     * @param blobName name of blob
     * @param splitPointCommit commit at split point
     * @param givenBranchHeadCommitFiles files in the given branch's head commit
     * @param givenBranchHeadCommit given branch's head commit
     * @param s bool s
     * @param c bool c
     * @param g bool g
     * @throws IOException
     */
    public void mergeHelper2(HashMap<String, Blob> currentBranchHeadCommitFiles,
                             String blobName, Commit splitPointCommit,
                             HashMap<String, Blob> givenBranchHeadCommitFiles,
                             Commit givenBranchHeadCommit,
                             Boolean s, Boolean c,
                             Boolean g) throws IOException {
        {
            if (g && c) {
                Blob gg =
                        givenBranchHeadCommitFiles.get(blobName);
                Blob cc =
                        currentBranchHeadCommitFiles.get(blobName);
                if (sameContent(gg, cc)) {
                    return;
                } else {
                    if (s) {
                        if (blobModifiedAfterSplitPoint(splitPointCommit,
                                blobName, headCommit)) {
                            if (blobModifiedAfterSplitPoint(
                                    splitPointCommit,
                                    blobName, givenBranchHeadCommit)) {
                                System.out.println("Encountered "
                                        + "a merge conflict.");
                                File blob = new File(blobName);
                                if (!blob.exists()) {
                                    blob.createNewFile();
                                }
                                Utils.writeContents(blob, "<<<<<<< HEAD\n",
                                        new String(cc.getContent(),
                                                StandardCharsets.UTF_8),
                                        "=======\n",
                                        new String(gg.getContent(),
                                                StandardCharsets.UTF_8),
                                        ">>>>>>>\n");

                                add(blobName);
                            } else {
                                return;
                            }
                        } else {
                            if (blobModifiedAfterSplitPoint(
                                    splitPointCommit,
                                    blobName, givenBranchHeadCommit)) {
                                checkOutFromSomeCommit("checkout",
                                        givenBranchHeadCommit.getId(),
                                        "--", blobName);
                                add(blobName);
                            } else {
                                return;
                            }
                        }
                    } else {
                        checkOutFromSomeCommit("checkout",
                                givenBranchHeadCommit.getId(),
                                "--", blobName);
                        add(blobName);
                    }
                }
            } else {
                mergeHelper3(currentBranchHeadCommitFiles, blobName,
                        splitPointCommit, givenBranchHeadCommitFiles,
                        givenBranchHeadCommit, s, c);
            }
        }
    }
    /**
     * Merge Helper 3.
     * @param currentBranchHeadCommitFiles current branch head commit files
     * @param blobName name of blob
     * @param splitPointCommit commit at split point
     * @param givenBranchHeadCommitFiles given branch head commit file
     * @param givenBranchHeadCommit head file of given branch
     * @param s boolean s
     * @param c bollean c
     * @throws IOException
     */
    public void mergeHelper3(HashMap<String, Blob> currentBranchHeadCommitFiles,
                             String blobName, Commit splitPointCommit,
                             HashMap<String, Blob> givenBranchHeadCommitFiles,
                             Commit givenBranchHeadCommit,
                             Boolean s, Boolean c) throws IOException {
        if (s) {
            if (c) {
                Blob cc =
                        currentBranchHeadCommitFiles.get(blobName);
                if (blobModifiedAfterSplitPoint(splitPointCommit,
                        blobName, headCommit)) {
                    System.out.println("Encountered "
                            + "a merge conflict.");
                    File blob = new File(blobName);
                    if (!blob.exists()) {
                        blob.createNewFile();
                    }
                    Utils.writeContents(blob, "<<<<<<< HEAD\n",
                            new String(cc.getContent(),
                                    StandardCharsets.UTF_8),
                            "=======\n", ">>>>>>>\n");
                    add(blobName);
                } else {
                    rm(blobName);
                }
            } else {
                Blob gg =
                        givenBranchHeadCommitFiles.get(blobName);
                if (blobModifiedAfterSplitPoint(splitPointCommit,
                        blobName, givenBranchHeadCommit)) {
                    System.out.println("Encountered "
                            + "a merge conflict.");
                    File blob = new File(blobName);
                    if (!blob.exists()) {
                        blob.createNewFile();
                    }
                    Utils.writeContents(blob, "<<<<<<< HEAD\n",
                            "=======\n", gg.getContent(),
                            ">>>>>>>\n");
                    add(blobName);
                } else {
                    return;
                }
            }
        } else {
            if (c) {
                return;
            } else {
                Blob newBlob =
                        givenBranchHeadCommitFiles.get(blobName);
                checkOutFromSomeCommit("checkout",
                        givenBranchHeadCommit.getId(),
                        "--", blobName);
                add(blobName);
            }
        }
    }
        /**
         * Check if blobname is a blob in this commit.
         * @param thisCommit some commit
         * @param blobname name of a blob
         * @return bool indicating if blob with blob name is in commit
         */
    public boolean blobInThisCommit(
            Commit thisCommit, String blobname) {
        HashMap<String, Blob> b = thisCommit.getBlobs();
        return b.containsKey(blobname);
    }

    /**
     * Check if blob is modifed after the split.
     * @param splitPointCommit split point of two branches
     * @param blobname name of a blob
     * @param me the current branch
     * @return boolean indicating if blob
     * with blobname has been modified
     */
    public boolean blobModifiedAfterSplitPoint(
            Commit splitPointCommit,
            String blobname, Commit me) {
        Blob blobInSplitPointCommit =
                splitPointCommit.getBlobs().get(blobname);
        Blob myBlob = me.getBlobs().get(blobname);
        String contentInSplitPoint = new
                String(blobInSplitPointCommit.getContent(),
                StandardCharsets.UTF_8);
        String myContent = new String(myBlob.getContent(),
                StandardCharsets.UTF_8);
        return !contentInSplitPoint.equals(myContent);
    }

    /**
     * Check if two blobs have the same content.
     * @param blob1 blob1
     * @param blob2 blob2
     * @return boolean indicating if blob1 and 2 are the same.
     */
    public boolean sameContent(Blob blob1, Blob blob2) {
        String content1 = new String(blob1.getContent(),
                StandardCharsets.UTF_8);
        String content2 = new String(blob2.getContent(),
                StandardCharsets.UTF_8);
        return content1.equals(content2);
    }


}
