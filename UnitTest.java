package gitlet;

import ucb.junit.textui;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import static org.junit.Assert.*;

import org.junit.Test;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Sara Wang
 */
public class UnitTest {



    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */


    @Test
    public void placeholderTest() {

    }

    @Test
    public void testBranch() throws IOException {


        Branch b = new Branch("master", null);
        Branch c = b;

        assertEquals(b.getId(), c.getId());

        Branch d = new Branch("master", null);

        assertEquals(b.getId(), d.getId());
        assertEquals("master", b.getName());

        HashMap<String, Blob> fakeBlobs = new HashMap<>();
        fakeBlobs.put("first", null);
        fakeBlobs.put("second", null);
        Commit a = new Commit("Unit", new Date(), fakeBlobs, null, true);

        b.add(a);

        assertEquals(b.getCommits().size(), 1);

        d.addAll(b);

        assertEquals(d.getCommits().size(), 1);

        HashMap<String, Blob> fakeBlobs2 = new HashMap<>();
        fakeBlobs2.put("one", null);
        fakeBlobs2.put("two", null);
        Commit e = new Commit("Test.", new Date(), fakeBlobs2, null, true);

        assertEquals(e.getParents(), null);

        b.add(e);

        assertEquals(b.getCommits().size(), 2);


    }

    @Test
    public void testCommit() throws IOException {
        HashMap<String, Blob> fakeBlobs = new HashMap<>();
        fakeBlobs.put("first", null);
        fakeBlobs.put("second", null);
        Commit c = new Commit("Unit test.", new Date(), fakeBlobs, null, true);

        assertEquals(c.getBlobs().keySet().size(), 2);
        assertEquals(c.getBlobs().get("first"), null);
        assertEquals(c.getBlobs().get("second"), null);

    }


    @Test
    public void testGetCommitMessage() throws IOException {
        HashMap<String, Blob> fakeBlobs = new HashMap<>();
        fakeBlobs.put("first", null);
        fakeBlobs.put("second", null);
        Commit c = new Commit("Unit test.", new Date(), fakeBlobs, null, true);

        assertEquals(c.getCommitMessage(), "Unit test.");

    }

    @Test
    public void testGetCommitDate() throws IOException {
        HashMap<String, Blob> fakeBlobs = new HashMap<>();
        fakeBlobs.put("first", null);
        fakeBlobs.put("second", null);
        Commit c = new Commit("Unit test.", new Date(0), fakeBlobs, null, true);

        assertEquals(c.getCommitDate(), new Date(0));

    }






}


