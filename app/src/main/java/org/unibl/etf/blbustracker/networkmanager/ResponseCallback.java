package org.unibl.etf.blbustracker.networkmanager;

import org.json.JSONObject;

/**Used as an arguemnt of GETJSON method in NetworkManager*/
public interface ResponseCallback
{
    void successResponse(JSONObject object);
}