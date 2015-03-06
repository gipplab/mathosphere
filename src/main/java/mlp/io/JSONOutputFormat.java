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
package mlp.io;

import org.apache.flink.api.java.record.io.DelimitedOutputFormat;
import org.apache.flink.types.Record;
import org.apache.flink.types.Value;

// meh … works for now! FIXME!!
public class JSONOutputFormat<T> extends DelimitedOutputFormat {

    @Override
    public int serializeRecord(Record record, byte[] target) throws Exception {
        @SuppressWarnings("unchecked")
        byte[] serialized = ((T) record.getField(0, Value.class)).toString().getBytes();
        System.arraycopy(serialized, 0, target, target.length, serialized.length);
        return serialized.length;
    }

}
