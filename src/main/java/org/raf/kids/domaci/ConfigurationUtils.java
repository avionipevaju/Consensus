package org.raf.kids.domaci;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigurationUtils {

    public static Node loadJsonConfiguration(String configurationUrl) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configurationUrl);
        String json = IOUtils.toString(fileInputStream);
        JSONObject jsonObject = new JSONObject(json);

        int id = jsonObject.getInt("id");
        int port = jsonObject.getInt("port");
        JSONArray neighbours = jsonObject.getJSONArray("neighbourNodes");

        ArrayList<Node> neighbourList = new ArrayList<>();
        for(Object object: neighbours) {
            JSONObject jo = (JSONObject) object;
            Node temp =  new Node(jo.getInt("id"), jo.getString("ipAddress"), jo.getInt("port"));
            neighbourList.add(temp);
        }

        return new Node(id, "127.0.0.1", port, neighbourList);

    }

}

