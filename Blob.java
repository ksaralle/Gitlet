package gitlet;
import java.io.Serializable;
import java.io.File;

/**
 * Created by Sara on 12/1/17.
 * @author Sara Wang
 */
public class Blob implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1234567L;

    /** The unique id of a blob. */
    private String id;

    /** The content of the blob. */
    private byte[] content;

    /** The name of the blob. */
    private String name;

    /** The files, have to actually exist.
     * @param filename name of the file used to created this blob */
    public Blob(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        this.id = Utils.sha1(Utils.serialize(this));

        this.name = filename;
        this.content = Utils.readContents(f);
    }

    /** Nonexistent files.
     * @param filename name of the file used to created this blob
     * @param nonexistent to distinguish from the real blobs */
    public Blob(String filename, Boolean nonexistent) {
        this.id = Utils.sha1(Utils.serialize(this));
        this.name = filename;

    }

    /** Returns the content of the file.
     * @return the content of the file in byte[]. */
    public byte[] getContent() {
        return content;
    }

}
