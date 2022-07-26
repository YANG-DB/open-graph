package org.opensearch.graph.test.scenario;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.action.bulk.BulkProcessor;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestGuildsToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = ETLUtils.getClient();
        BulkProcessor processor = ETLUtils.getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\guilds.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("name", CsvSchema.ColumnType.STRING)
                        .addColumn("description", CsvSchema.ColumnType.STRING)
                        .addColumn("iconId", CsvSchema.ColumnType.STRING)
                        .addColumn("url", CsvSchema.ColumnType.STRING)
                        .addColumn("establishDate", CsvSchema.ColumnType.NUMBER)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {});

        String index = "misc";
        String type = ETLUtils.GUILD;


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> guild = reader.readValue(line);
                String id = ETLUtils.id(type, guild.remove("id").toString());

                guild.put(ETLUtils.ESTABLISH_DATE, ETLUtils.sdf.format(new Date(Long.parseLong(guild.get("establishDate").toString()))));

                processor.add(new IndexRequest(index, type, id).source(guild));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }
}
