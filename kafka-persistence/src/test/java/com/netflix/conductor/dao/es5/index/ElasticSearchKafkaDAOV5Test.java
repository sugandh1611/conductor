package com.netflix.conductor.dao.es5.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.dao.ProducerDAO;
import com.netflix.conductor.dao.kafka.index.utils.DataUtils;
import com.netflix.conductor.elasticsearch.ElasticSearchConfiguration;
import com.netflix.conductor.elasticsearch.SystemPropertiesElasticSearchConfiguration;
import org.elasticsearch.client.Client;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static com.netflix.conductor.elasticsearch.ElasticSearchConfiguration.ELASTIC_SEARCH_ASYNC_DAO_MAX_POOL_SIZE;
import static com.netflix.conductor.elasticsearch.ElasticSearchConfiguration.ELASTIC_SEARCH_ASYNC_DAO_WORKER_QUEUE_SIZE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class ElasticSearchKafkaDAOV5Test {

    private static ElasticSearchDAOV5 indexDAO;
    private static Client restClient;
    private static ElasticSearchConfiguration configuration;
    private static ObjectMapper objectMapper;
    private static ProducerDAO producerDAO;



    @BeforeClass
    public static void start() throws Exception {
        restClient = Mockito.mock(Client.class);
        System.setProperty(ELASTIC_SEARCH_ASYNC_DAO_MAX_POOL_SIZE, "6");
        System.setProperty(ELASTIC_SEARCH_ASYNC_DAO_WORKER_QUEUE_SIZE, "1");

        configuration = new SystemPropertiesElasticSearchConfiguration();
        objectMapper = Mockito.mock(ObjectMapper.class);
        producerDAO = Mockito.mock(ProducerDAO.class);
        indexDAO = new ElasticSearchKafkaDAOV5(restClient, configuration, objectMapper, producerDAO);
        Mockito.doNothing().when(producerDAO).send(any(String.class), any(Object.class));
    }

    @Test
    public void testIndexWorkflow() {
        Workflow workflow = new Workflow();
        indexDAO.indexWorkflow(workflow);
        Mockito.verify(producerDAO).send(eq(DataUtils.WORKFLOW_DOC_TYPE), any(Object.class));
    }

    @Test
    public void testIndexTask() {
        Task task = new Task();
        indexDAO.indexTask(task);
        Mockito.verify(producerDAO).send(eq(DataUtils.TASK_DOC_TYPE), any(Object.class));
    }
}
