package de.ronnyfriedland.knowledgebase.repository;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;

import de.ronnyfriedland.knowledgebase.entity.Document;
import de.ronnyfriedland.knowledgebase.exception.DataException;

/**
 * @author ronnyfriedland
 */
@org.springframework.stereotype.Repository
@org.springframework.beans.factory.annotation.Qualifier("jcr")
public class JackRabbitRepository implements IRepository {

    public static final String PROPERTY_HEADER = "header";
    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_TAGS = "tags";

    private Repository repository;

    @PostConstruct
    public void init() {
        try {
            repository = JcrUtils.getRepository();
        } catch (RepositoryException e) {
            throw new de.ronnyfriedland.knowledgebase.exception.RepositoryException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see de.ronnyfriedland.knowledgebase.repository.IRepository#getTextDocument(java.lang.String)
     */
    @Override
    public Document<String> getTextDocument(final String key) throws DataException {
        try {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            try {
                Node root = session.getRootNode();
                Node node = root.getNode(key);

                return new Document<String>(key, node.getProperty(PROPERTY_HEADER).getString(), node.getProperty(
                        PROPERTY_MESSAGE).getString());
            } finally {
                session.logout();
            }
        } catch (RepositoryException e) {
            throw new DataException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see de.ronnyfriedland.knowledgebase.repository.IRepository#saveTextDocument(de.ronnyfriedland.knowledgebase.entity.Document)
     */
    @Override
    public String saveTextDocument(final Document<String> message) throws DataException {
        try {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            Node node = null;
            try {
                Node root = session.getRootNode();

                if (!root.hasNode(message.getKey())) {
                    node = root.addNode(message.getKey());
                } else {
                    node = root.getNode(message.getKey());
                }

                node.setProperty(PROPERTY_HEADER, message.getHeader());
                node.setProperty(PROPERTY_MESSAGE, message.getMessage());
                node.setProperty(PROPERTY_TAGS, message.getTags());
                session.save();
            } finally {
                session.logout();
            }
            return node.getIdentifier();
        } catch (RepositoryException e) {
            throw new DataException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see de.ronnyfriedland.knowledgebase.repository.IRepository#listTextDocuments(int, int)
     */
    @Override
    public Collection<Document<String>> listTextDocuments(final int offset, final int max) throws DataException {
        Collection<Document<String>> result = new ArrayList<>();
        try {
            Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            try {
                // QueryManager qm = session.getWorkspace().getQueryManager();
                // QueryObjectModelFactory qomf = qm.getQOMFactory();
                // Selector nodeTypeSelector = qomf.selector("unstructured", "unstructured" + "Selector");
                //
                // QueryObjectModel qom = qomf.createQuery(nodeTypeSelector, null, null, null);
                //
                // qom.setLimit(max);
                // qom.setOffset(offset);
                // QueryResult qr = qom.execute();

                Node root = session.getRootNode();

                NodeIterator nodes = root.getNodes();
                while (nodes.hasNext()) {
                    Node node = (Node) nodes.next();
                    if (node.hasProperty(PROPERTY_HEADER)) {
                        result.add(new Document<String>(node.getName(), node.getProperty(PROPERTY_HEADER).getString(),
                                node.getProperty(PROPERTY_MESSAGE).getString()));
                    }
                }

            } finally {
                session.logout();
            }
        } catch (RepositoryException e) {
            throw new DataException(e);
        }
        return result;
    }
}
