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

import org.apache.flink.types.ListValue;
import org.apache.flink.types.StringValue;

/**
 * @author rob
 */
public class Identifiers extends ListValue<StringValue> {

    public boolean containsIdentifier(String identifier) {
        for (StringValue i : this) {
            if (identifier.equals(i.getValue())) {
                return true;
            }
        }
        return false;
    }
}
