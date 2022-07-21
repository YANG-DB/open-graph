package org.opensearch.graph.executor.opensearch.logging;



import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.opensearch.graph.dispatcher.logging.LogMessage;
import org.slf4j.MDC;

public class EngineResults {

    public static HitsWriter hitsWriter(long hits) {
        return new HitsWriter(hits);
    }

    public static TotalHitsWriter totalHitsWriter(long totalHits) {
        return new TotalHitsWriter(totalHits);
    }

    public static ShardsWriter shardsWrite(int shards) {
        return new ShardsWriter(shards);
    }

    public static ScrollIdWriter scrollIdWriter(String scrollId) {
        return new ScrollIdWriter(scrollId);
    }


    public static class HitsWriter implements LogMessage.MDCWriter {
        //region Constructors
        public HitsWriter(long hits) {
            this.hits = hits;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(HitsConverter.key, Long.toString(this.hits));
        }

        //endregion
        //region Fields
        private long hits;
        //endregion
    }

    public static class TotalHitsWriter implements LogMessage.MDCWriter {
        public static final String key = "totalHits";

        //region Constructors
        public TotalHitsWriter(long totalHits) {
            this.totalHits = totalHits;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(TotalHitsConverter.key, Long.toString(totalHits));
        }
        //endregion

        //region Fields
        private long totalHits;
        //endregion
    }

    public static class ShardsWriter implements LogMessage.MDCWriter {
        //region Constructors
        public ShardsWriter(int shards) {
            this.shards = shards;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(TotalShardsConverter.key, Long.toString(this.shards));
        }
        //endregion

        //region Fields
        private int shards;
        //endregion
    }

    public static class ScrollIdWriter implements LogMessage.MDCWriter {
        //region Constructors
        public ScrollIdWriter(String scrollId) {
            this.scrollId = scrollId;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            if (scrollId != null) {
                MDC.put(ScrollIdConverter.key, this.scrollId);
            }
        }
        //endregion

        //region Fields
        private String scrollId;
        //endregion
    }


    public static class HitsConverter extends ClassicConverter {
        public static final String key = "hits";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class TotalHitsConverter extends ClassicConverter {
        public static final String key = "totalHits";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class TotalShardsConverter extends ClassicConverter {
        public static final String key = "totalShards";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class ScrollIdConverter extends ClassicConverter {
        public static final String key = "scrollId";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }
}
