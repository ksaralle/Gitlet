package gitlet;





import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sara on 12/1/17.
 * @author Sara Wang
 */
public class Branch implements Serializable {




    /** Name of the branch. */
    private String branchName;

    /** ArrayList of all commits in this branch. */
    private ArrayList<Commit> commits;

    /** Head of the branch. */
    private Commit headOfBranch;

    /** Unique id of this branch. */
    private String id;

    /** Represents a branch in the commit tree.
     * @param name name of this branch
     * @param head head of this branch */
    public Branch(String name, Commit head) {
        this.branchName = name;
        this.headOfBranch = head;
        this.commits = new ArrayList<>();
        this.id = Utils.sha1(Utils.serialize(this));
    }

    /** Adds new commits to this branch.
     * @param c new commits to be added */
    public void add(Commit c) {
        commits.add(c);
        headOfBranch = c;
    }

    /** Adds all previous commits when making a new branch.
     * @param b the branch that this branch branched from */
    public void addAll(Branch b) {
        commits.addAll(b.getCommits());
        headOfBranch = b.getHead();
    }

    /** Returns the name of this branch.
     * @return name of the branch */
    public String getName() {
        return branchName;
    }

    /** Returns all the commits of this branch.
     * @return commits */
    public ArrayList<Commit> getCommits() {
        return commits;
    }

    /** Change the head of the branch.
     * @param c new commit head */
    public void changeHead(Commit c) {
        headOfBranch = c;
    }

    /** Returns the head commit of the branch.
     * @return head of branch */
    public Commit getHead() {
        return headOfBranch;
    }


    /** Returns the id of this branch.
     * @return id */
    public String getId() {
        return id;
    }



}
