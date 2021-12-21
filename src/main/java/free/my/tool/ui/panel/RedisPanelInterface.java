package free.my.tool.ui.panel;

import java.util.List;

public interface RedisPanelInterface {

    void connectRedis(String host, String port, String password, String database, String numOfDatabase, boolean applyBase64Encoding);

    void disconnectRedis();

    List<String> getKeyList(boolean isRefreshButtonClicked, String searchKey);

    void searchKey(String searchKey);

}