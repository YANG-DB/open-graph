package org.opensearch.graph.services.controllers.logging;




import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.dispatcher.decorators.MethodDecorator;
import org.opensearch.graph.dispatcher.logging.Elapsed;
import org.opensearch.graph.dispatcher.logging.LogMessage;
import org.opensearch.graph.logging.RequestExternalMetadata;
import org.opensearch.graph.logging.RequestId;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

public abstract class LoggingControllerBase<TController> {
    //general purpose mapper to be used by any individual logger
    protected static ObjectMapper mapper = new ObjectMapper();

    //region Constructors
    public LoggingControllerBase(
            TController controller,
            Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.requestExternalMetadataSupplier = requestExternalMetadataSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region Protected Methods
    protected LogMessage.MDCWriter primerMdcWriter() {
        return LogMessage.MDCWriter.Composite.of(
                Elapsed.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()));
    }

    protected <TResult> MethodDecorator.ResultHandler<ContentResponse<TResult>, Long> resultHandler() {
        return new MethodDecorator.ResultHandler<ContentResponse<TResult>, Long>() {
            @Override
            public ContentResponse<TResult> onSuccess(ContentResponse<TResult> response, Long elapsed) {
                return ContentResponse.Builder.builder(response)
                        .requestId(requestIdSupplier.get())
                        .external(requestExternalMetadataSupplier.get())
                        .elapsed(elapsed)
                        .compose();
            }

            @Override
            public ContentResponse<TResult> onFailure(Exception ex, Long elapsed) {
                return onSuccess(ContentResponse.internalError(ex), elapsed);
            }
        };
    }
    //endregion

    //region Fields
    protected TController controller;
    protected Logger logger;
    protected RequestIdSupplier requestIdSupplier;
    protected RequestExternalMetadataSupplier requestExternalMetadataSupplier;
    protected MetricRegistry metricRegistry;
    //endregion
}
