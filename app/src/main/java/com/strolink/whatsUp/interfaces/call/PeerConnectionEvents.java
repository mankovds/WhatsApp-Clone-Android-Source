package com.strolink.whatsUp.interfaces.call;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

/**
 * Created by Abderrahim El imame on 5/17/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public interface PeerConnectionEvents {
    void onIceCandidate(IceCandidate iceCandidate);

    void onIceCandidatesRemoved(final IceCandidate[] candidates);

    void onIceConnected();

    void onIceDisconnected();

    void onLocalDescription(SessionDescription sessionDescription);

    void onPeerConnectionClosed();

    void onPeerConnectionError(String str);

    void onPeerConnectionStatsReady(StatsReport[] statsReportArr);
}
