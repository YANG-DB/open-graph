package org.opensearch.graph.dispatcher.driver;




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;

public abstract class CursorDriverBase implements CursorDriver {
    public static final String CONTEXT = "context";
    //region Constructors
    @Inject
    public CursorDriverBase(MetricRegistry registry, ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.registry = registry;
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;

    }
    //endregion

    //region CursorDriver Implementation
    @Override
    public Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest cursorRequest) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
                return Optional.of(new CursorResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "failed fetching next page for query " + queryId)));
        }
        //outer query cursor id
        String cursorId = queryResource.get().getNextCursorId();
        //inner cursors for inner queries
        createInnerCursor(queryResource.get(),cursorRequest);
        CursorResource resource = this.createResource(queryResource.get(), cursorId, cursorRequest);
        this.resourceStore.addCursorResource(queryId, resource);

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorId,
                cursorRequest,
                urlSupplier.pageStoreUrl(queryId, cursorId)));
    }

    private void createInnerCursor(QueryResource query, CreateCursorRequest cursorRequest) {
        Iterable<QueryResource> innerQueryResources = query.getInnerQueryResources();
        innerQueryResources.forEach(innerQuery->{
            create(innerQuery.getQueryMetadata().getId(),cursorRequest);
        });
    }

    @Override
    public Optional<StoreResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Iterable<String> resourceUrls = Stream.ofAll(queryResource.get().getCursorResources())
                .sortBy(CursorResource::getTimeCreated)
                .map(CursorResource::getCursorId)
                .map(cursorId -> this.urlSupplier.resourceUrl(queryId, cursorId))
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.cursorStoreUrl(queryId),queryId, resourceUrls));
    }

    @Override
    public Optional<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorId,
                cursorResource.get().getCursorRequest(),
                cursorResource.get().getProfileInfo().infoData(cursorResource.get().getCursorRequest()),
                urlSupplier.pageStoreUrl(queryId, cursorId),
                resourceStore.getPageResource(queryId,cursorId,cursorResource.get().getCurrentPageId()).isPresent() ?
                        Collections.singletonList(
                                pageDriver.getInfo(queryId,cursorId,cursorResource.get().getCurrentPageId()).get()) :
                        Collections.EMPTY_LIST
                ));
    }


    @Override
    public Optional<Boolean> delete(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }
        //try delete inner cursors
        queryResource.get().getInnerQueryResources().forEach(inner->delete(inner.getQueryMetadata().getId(),cursorId));
        //remove any existing underlying open scrolls
        queryResource.get().getCursorResource(cursorId).get().clearScrolls();
        //delete outer cursor
        queryResource.get().deleteCursorResource(cursorId);
        return Optional.of(true);
    }


    //endregion

    //region Protected Abstract Methods
    protected abstract CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest);
    //endregion

    //region Fields
    protected PageDriver pageDriver;
    protected MetricRegistry registry;
    protected ResourceStore resourceStore;
    protected AppUrlSupplier urlSupplier;
    //endregion
}
