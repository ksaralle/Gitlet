package gitlet;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A commit object
 * Created by Sara on 12/1/17.
 * @author Sara Wang
 */
public class Commit implements Serializable {

    /** Commit Message of the commit. */
    private String commitMessage;

    /** Creation date. */
    private Date commitDate;

    /** Filename to Blob mapping. */
    private HashMap<String, Blob> blobsOfCommit;

    /** List of parents. */
    private ArrayList<Commit> parentsOfCommit;

    /** Long UID. */
    private static final long serialVersionUID = 12345678L;

    /** Commit ID. */
    private String _id;

    /** Create a commit.
     * @param messages commit message
     * @param date commit date
     * @param blobs blobs in this commit
     * @param parents the parent commits */
    public Commit(String messages, Date date, HashMap<String, Blob> blobs,
                  ArrayList<Commit> parents)
            throws IOException {
        if (messages == null || messages.isEmpty()
                || messages.equals("")) {
            throw new IllegalArgumentException(
                    "Please enter a commit message.");
        }
        this.commitMessage = messages;
        this.commitDate = date;
        this.blobsOfCommit = blobs;
        this.parentsOfCommit = parents;
        this._id = Utils.sha1(Utils.serialize(this));
        save();
    }

    /** Special constructor for initial commit. */
    public Commit() throws IOException {
        this.commitMessage = "initial commit";
        this.commitDate = new Date(0);
        this.blobsOfCommit = new HashMap<String, Blob>();
        this.parentsOfCommit = null;
        this._id = Utils.sha1(Utils.serialize(this));
        save();
    }

    /** Special constructor for unit test.
     * @param messages commit message
     * @param date commit date
     * @param blobs blobs in this commit
     * @param parents the parent commits
     * @param unitTest indicate that this is for unit test only */
    public Commit(String messages, Date date, HashMap<String, Blob> blobs,
                  ArrayList<Commit> parents, boolean unitTest)
            throws IOException {
        this.commitMessage = messages;
        this.commitDate = date;
        this.blobsOfCommit = blobs;
        this.parentsOfCommit = parents;
        this._id = Utils.sha1(Utils.serialize(this));
    }

    /** Get commit message.
     * @return commit message */
    public String getCommitMessage() {
        return this.commitMessage;
    }

    /** Get commit date.
     * @return commit date */
    public Date getCommitDate() {
        return this.commitDate;
    }

    /** Get blobs.
     * @return the blobs */
    public HashMap<String, Blob> getBlobs() {
        return this.blobsOfCommit;
    }

    /** Get the parents of this commit.
     * @return the parents */
    public ArrayList<Commit> getParents() {
        return this.parentsOfCommit;
    }

    /** Get the commit id.
     * @return commit id */
    public String getId() {
        return _id;
    }

    /** Serialize the commit. */
    public void save() throws IOException {
        File commitFile = new File(".gitlet/commit/" + _id);
        commitFile.createNewFile();
        Utils.writeObject(commitFile, this);
    }
}
