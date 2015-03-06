/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package mlp.types;

import java.io.IOException;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.StringValue;
import org.apache.flink.types.Value;

/**
 * @author rob
 */
public class Formula implements Value {

    private final StringValue hash = new StringValue();
    private final StringValue src = new StringValue();

    public Formula() {
    }

    public Formula(String hash, String src) {
        this.hash.setValue(hash);
        this.src.setValue(src);
    }

    public String getHash() {
        return hash.getValue();
    }

    public void setHash(String string) {
        hash.setValue(string);
    }

    public String getSrc() {
        return src.getValue();
    }

    public void setSrc(String string) {
        src.setValue(string);
    }

    @Override
    public String toString() {
        return this.getHash();
    }

    @Override
    public void write(DataOutputView out) throws IOException {
        hash.write(out);
        src.write(out);
    }

    @Override
    public void read(DataInputView in) throws IOException {
        hash.read(in);
        src.read(in);
    }

}
