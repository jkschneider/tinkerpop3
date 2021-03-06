package com.tinkerpop.gremlin.server.channel;

import com.tinkerpop.gremlin.groovy.engine.GremlinExecutor;
import com.tinkerpop.gremlin.server.AbstractChannelizer;
import com.tinkerpop.gremlin.server.Graphs;
import com.tinkerpop.gremlin.server.Settings;
import com.tinkerpop.gremlin.server.handler.WsGremlinBinaryRequestDecoder;
import com.tinkerpop.gremlin.server.handler.WsGremlinResponseEncoder;
import com.tinkerpop.gremlin.server.handler.WsGremlinTextRequestDecoder;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link com.tinkerpop.gremlin.server.Channelizer} that exposes a WebSocket-based Gremlin endpoint with a custom
 * sub-protocol.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class WebSocketChannelizer extends AbstractChannelizer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketChannelizer.class);

    private WsGremlinResponseEncoder wsGremlinResponseEncoder;
    private WsGremlinTextRequestDecoder wsGremlinTextRequestDecoder;
    private WsGremlinBinaryRequestDecoder wsGremlinBinaryRequestDecoder;

    @Override
    public void init(final Settings settings, final GremlinExecutor gremlinExecutor,
                     final ExecutorService gremlinExecutorService, final Graphs graphs,
                     final ScheduledExecutorService scheduledExecutorService) {
        super.init(settings, gremlinExecutor, gremlinExecutorService, graphs, scheduledExecutorService);

        wsGremlinResponseEncoder = new WsGremlinResponseEncoder();
        wsGremlinTextRequestDecoder = new WsGremlinTextRequestDecoder();
        wsGremlinBinaryRequestDecoder = new WsGremlinBinaryRequestDecoder(serializers);
    }

    @Override
    public void configure(final ChannelPipeline pipeline) {
        if (logger.isDebugEnabled())
            pipeline.addLast(new LoggingHandler("log-io", LogLevel.DEBUG));

        logger.debug("HttpRequestDecoder settings - maxInitialLineLength={}, maxHeaderSize={}, maxChunkSize={}",
                settings.maxInitialLineLength, settings.maxHeaderSize, settings.maxChunkSize);
        pipeline.addLast("http-request-decoder", new HttpRequestDecoder(settings.maxInitialLineLength, settings.maxHeaderSize, settings.maxChunkSize));

        if (logger.isDebugEnabled())
            pipeline.addLast(new LoggingHandler("log-decoder-aggregator", LogLevel.DEBUG));

        logger.debug("HttpObjectAggregator settings - maxContentLength={}, maxAccumulationBufferComponents={}",
                settings.maxContentLength, settings.maxAccumulationBufferComponents);
        final HttpObjectAggregator aggregator = new HttpObjectAggregator(settings.maxContentLength);
        aggregator.setMaxCumulationBufferComponents(settings.maxAccumulationBufferComponents);
        pipeline.addLast("aggregator", aggregator);

        if (logger.isDebugEnabled())
            pipeline.addLast(new LoggingHandler("log-aggregator-encoder", LogLevel.DEBUG));

        pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
        pipeline.addLast("request-handler", new WebSocketServerProtocolHandler("/gremlin", null, false, settings.maxContentLength));

        if (logger.isDebugEnabled())
            pipeline.addLast(new LoggingHandler("log-aggregator-encoder", LogLevel.DEBUG));

        pipeline.addLast("response-encoder", wsGremlinResponseEncoder);
        pipeline.addLast("request-text-decoder", wsGremlinTextRequestDecoder);
        pipeline.addLast("request-binary-decoder", wsGremlinBinaryRequestDecoder);

        if (logger.isDebugEnabled())
            pipeline.addLast(new LoggingHandler("log-aggregator-encoder", LogLevel.DEBUG));
    }
}