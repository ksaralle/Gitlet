package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Sara Wang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    /** Check if the input command has wrong number or format of operands.
     * @param num the supposed number of operands
     * @param args the input command */
    public static void testLength(int num, String... args) {
        if (args.length != num) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Reconstruct the commit tree to retrieve previous information.
     * @return the commit tree with all information stored */
    public static CommitTree deserializeCommitTree() throws IOException {

        File commitTreeFile = new File(".gitlet/commitTree.ser");

        if (commitTreeFile.exists()) {
            return Utils.readObject(commitTreeFile, CommitTree.class);
        } else {
            return new CommitTree();
        }
    }

    /** Save all information in a commit tree for future reference.
     * @param commitTree stores all information */
    public static void serializeCommitTree(CommitTree commitTree) {

        try {
            File commitTreeFile = new File(".gitlet/commitTree.ser");
            if (!commitTreeFile.exists()) {
                commitTreeFile.createNewFile();
            }
            Utils.writeObject(commitTreeFile, commitTree);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /** Reconstruct the commit tree to retrieve previous information.
     * @return the commit tree with all information stored */
    public static CommitTree deserializeRemote() throws IOException {

        File commitTreeFile = new File(".gitlet/remote.ser");

        if (commitTreeFile.exists()) {
            return Utils.readObject(commitTreeFile, CommitTree.class);
        } else {
            return new CommitTree();
        }
    }

    /** Save all information in a commit tree for future reference.
     * @param commitTree stores all information */
    public static void serializeRemote(CommitTree commitTree) {

        try {
            File commitTreeFile = new File(".gitlet/remote.ser");
            if (!commitTreeFile.exists()) {
                commitTreeFile.createNewFile();
            }
            Utils.writeObject(commitTreeFile, commitTree);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /** Parse the commands and execute accordingly.
     * @param gitlet all information
     * @param args input commands */
    public static void command(CommitTree gitlet, String... args)
            throws IOException {
        String command = args[0];
        File metaData = new File(".gitlet");
        if (!metaData.exists() && !command.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch (command) {
        case "init":
            testLength(1, args); gitlet.commitInit(); break;
        case "add":
            testLength(2, args); gitlet.add(args[1]); break;
        case "commit":
            String message = "";
            if (args.length > 1) {
                message = args[1];
            }
            gitlet.commit(message, null); break;
        case "rm":
            testLength(2, args); gitlet.rm(args[1]); break;
        case "log":
            testLength(1, args); gitlet.log(); break;
        case "global-log":
            testLength(1, args); gitlet.globalLog(); break;
        case "find":
            testLength(2, args); gitlet.find(args[1]); break;
        case "status":
            testLength(1, args); gitlet.status(); break;
        case "checkout":
            if (args.length == 2) {
                gitlet.checkOutFromBranch(args[1]);
            } else if (args.length == 3) {
                gitlet.checkOutFromHeadCommit(args);
            } else if (args.length == 4) {
                gitlet.checkOutFromSomeCommit(args);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            break;
        case "branch":
            testLength(2, args); gitlet.branch(args[1]); break;
        case "rm-branch":
            testLength(2, args); gitlet.rmBranch(args[1]); break;
        case "reset":
            testLength(2, args); gitlet.reset(args[1]); break;
        case "merge":
            testLength(2, args); gitlet.merge(args[1]); break;
        case "add-remote":
            testLength(3, args); gitlet.addRemote(args[1], args[2]); break;
        case "rm-remote":
            testLength(2, args); gitlet.rmRemote(args[1]); break;
        case "push":
            testLength(3, args); gitlet.push(args[1], args[2]); break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0); break;
        }
    }


    /** Run gitlet.
     * @param args the command */
    public static void main(String... args) throws IOException {
        if (args == null || args.length < 1) {
            System.err.println("Please enter a command.");
            System.exit(0);
        }
        ArrayList<String> remoteCommands = new ArrayList<>();
        remoteCommands.add("add-remote");
        remoteCommands.add("rm-remote");
        remoteCommands.add("push");
        remoteCommands.add("fetch");
        remoteCommands.add("pull");

        CommitTree gitlet = deserializeCommitTree();
        command(gitlet, args);

        if (remoteCommands.contains(args[0])) {
            CommitTree remote = deserializeRemote();
            command(remote, args);
            serializeRemote(remote);
        }
        serializeCommitTree(gitlet);

    }
}
