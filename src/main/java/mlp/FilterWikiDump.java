package mlp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.core.fs.FSDataInputStream;

public class FilterWikiDump {

    public static void main(String[] args) {

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        InputFormat inputFormat = null;
        env.createInput(inputFormat);

        // TODO Auto-generated method stub

    }

    /**
     * copied from InflaterInputStreamFSInputWrapper
     */
    public static class CompressedFSDataInputStream extends FSDataInputStream {

        private final BufferedInputStream stream;

        public static CompressedFSDataInputStream wrap(InputStream is) throws CompressorException {
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(is);
            BufferedInputStream bis = new BufferedInputStream(input);
            return new CompressedFSDataInputStream(bis);
        }

        private CompressedFSDataInputStream(BufferedInputStream stream) {
            this.stream = stream;
        }

        @Override
        public void seek(long desired) throws IOException {
            throw new UnsupportedOperationException("Compressed streams do not support the seek operation");
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return stream.read(b);
        }
    }

}
