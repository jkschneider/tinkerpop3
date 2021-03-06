package com.tinkerpop.gremlin.hadoop.structure.io.graphson;

import com.tinkerpop.gremlin.hadoop.structure.io.VertexWritable;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphSONRecordWriter extends RecordWriter<NullWritable, VertexWritable> {
    private static final String UTF8 = "UTF-8";
    private static final byte[] NEWLINE;
    private final DataOutputStream out;
    private static final GraphSONWriter GRAPHSON_WRITER = GraphSONWriter.build().create();

    static {
        try {
            NEWLINE = "\n".getBytes(UTF8);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException("Can not find " + UTF8 + " encoding");
        }
    }

    public GraphSONRecordWriter(final DataOutputStream out) {
        this.out = out;
    }

    @Override
    public void write(final NullWritable key, final VertexWritable vertex) throws IOException {
        if (null != vertex) {
            GRAPHSON_WRITER.writeVertex(out, vertex.get(), Direction.BOTH);
            this.out.write(NEWLINE);
        }
    }

    @Override
    public synchronized void close(TaskAttemptContext context) throws IOException {
        this.out.close();
    }
}
