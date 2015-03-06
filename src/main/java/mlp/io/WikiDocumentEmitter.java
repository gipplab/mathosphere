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

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;

/**
 * @author rob
 */
//TODO: remove it
public class WikiDocumentEmitter extends TextInputFormat {

    public WikiDocumentEmitter(Path filePath) {
        super(filePath);
    }

    @Override
    public void configure(Configuration parameters) {
        setCharsetName("UTF-8");
        super.configure(parameters);
        setDelimiter("</page>");
    }
}