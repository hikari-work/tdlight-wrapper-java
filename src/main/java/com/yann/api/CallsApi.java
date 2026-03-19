package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Reactive wrapper for TDLib voice call and group call (video chat) operations.
 */
public final class CallsApi extends TdlightOperations {

    public CallsApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Voice calls (1-to-1)
    // ------------------------------------------------------------------

    /** Create an outgoing call to a user. */
    public Mono<TdApi.CallId> createCall(long userId, TdApi.CallProtocol protocol, boolean isVideo) {
        TdApi.CreateCall req = new TdApi.CreateCall();
        req.userId = userId;
        req.protocol = protocol;
        req.isVideo = isVideo;
        return send(req);
    }

    /** Accept an incoming call. */
    public Mono<TdApi.Ok> acceptCall(int callId, TdApi.CallProtocol protocol) {
        TdApi.AcceptCall req = new TdApi.AcceptCall();
        req.callId = callId;
        req.protocol = protocol;
        return send(req);
    }

    /** Discard / hang up a call. */
    public Mono<TdApi.Ok> discardCall(int callId, boolean isDisconnected, int duration,
                                       boolean isVideo, long connectionId) {
        TdApi.DiscardCall req = new TdApi.DiscardCall();
        req.callId = callId;
        req.isDisconnected = isDisconnected;
        req.duration = duration;
        req.isVideo = isVideo;
        req.connectionId = connectionId;
        return send(req);
    }

    /** Send a rating for a finished call. */
    public Mono<TdApi.Ok> sendCallRating(int callId, int rating, String comment,
                                          TdApi.CallProblem[] problems) {
        TdApi.SendCallRating req = new TdApi.SendCallRating();
        req.callId = callId;
        req.rating = rating;
        req.comment = comment;
        req.problems = problems != null ? problems : new TdApi.CallProblem[0];
        return send(req);
    }

    /** Send debug information for a finished call. */
    public Mono<TdApi.Ok> sendCallDebugInformation(int callId, String debugInformation) {
        TdApi.SendCallDebugInformation req = new TdApi.SendCallDebugInformation();
        req.callId = callId;
        req.debugInformation = debugInformation;
        return send(req);
    }

    /** Send signaling data during a call (WebRTC). */
    public Mono<TdApi.Ok> sendCallSignalingData(int callId, byte[] data) {
        TdApi.SendCallSignalingData req = new TdApi.SendCallSignalingData();
        req.callId = callId;
        req.data = data;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Group calls
    // ------------------------------------------------------------------

    /**
     * Create a new group call (standalone, not tied to a chat video chat).
     * Returns {@link TdApi.GroupCallInfo} containing the call id and join payload.
     *
     * @param params join parameters (audio source id, payload, muted state, video state)
     */
    public Mono<TdApi.GroupCallInfo> createGroupCall(TdApi.GroupCallJoinParameters params) {
        TdApi.CreateGroupCall req = new TdApi.CreateGroupCall();
        req.joinParameters = params;
        return send(req);
    }

    /**
     * Convenience builder for {@link TdApi.GroupCallJoinParameters}.
     *
     * @param audioSourceId SSRC / audio source id
     * @param payload       JSON payload from the WebRTC signaling
     * @param isMuted       whether to join muted
     * @param isVideoEnabled whether to join with video enabled
     */
    public static TdApi.GroupCallJoinParameters joinParams(int audioSourceId, String payload,
                                                            boolean isMuted, boolean isVideoEnabled) {
        TdApi.GroupCallJoinParameters p = new TdApi.GroupCallJoinParameters();
        p.audioSourceId = audioSourceId;
        p.payload = payload;
        p.isMuted = isMuted;
        p.isMyVideoEnabled = isVideoEnabled;
        return p;
    }

    /** Get information about a group call. */
    public Mono<TdApi.GroupCall> getGroupCall(int groupCallId) {
        TdApi.GetGroupCall req = new TdApi.GetGroupCall();
        req.groupCallId = groupCallId;
        return send(req);
    }

    /**
     * Join an existing group call.
     * Returns {@link TdApi.GroupCallInfo} with the server answer payload.
     *
     * @param inputGroupCall reference to the group call (e.g. from chat video chat)
     * @param params         join parameters built with {@link #joinParams}
     */
    public Mono<TdApi.GroupCallInfo> joinGroupCall(TdApi.InputGroupCall inputGroupCall,
                                                    TdApi.GroupCallJoinParameters params) {
        TdApi.JoinGroupCall req = new TdApi.JoinGroupCall();
        req.inputGroupCall = inputGroupCall;
        req.joinParameters = params;
        return send(req);
    }

    /** Leave a group call (client side; call continues for others). */
    public Mono<TdApi.Ok> leaveGroupCall(int groupCallId) {
        TdApi.LeaveGroupCall req = new TdApi.LeaveGroupCall();
        req.groupCallId = groupCallId;
        return send(req);
    }

    /** End a group call (admin only; terminates the call for everyone). */
    public Mono<TdApi.Ok> endGroupCall(int groupCallId) {
        TdApi.EndGroupCall req = new TdApi.EndGroupCall();
        req.groupCallId = groupCallId;
        return send(req);
    }

    /** Start recording a group call. */
    public Mono<TdApi.Ok> startGroupCallRecording(int groupCallId, String title,
                                                    boolean recordVideo,
                                                    boolean usePortraitOrientation) {
        TdApi.StartGroupCallRecording req = new TdApi.StartGroupCallRecording();
        req.groupCallId = groupCallId;
        req.title = title;
        req.recordVideo = recordVideo;
        req.usePortraitOrientation = usePortraitOrientation;
        return send(req);
    }

    /** Stop recording a group call. */
    public Mono<TdApi.Ok> endGroupCallRecording(int groupCallId) {
        TdApi.EndGroupCallRecording req = new TdApi.EndGroupCallRecording();
        req.groupCallId = groupCallId;
        return send(req);
    }

    /** Mute or unmute a participant in a group call. */
    public Mono<TdApi.Ok> toggleGroupCallParticipantIsMuted(int groupCallId,
                                                              TdApi.MessageSender participantId,
                                                              boolean isMuted) {
        TdApi.ToggleGroupCallParticipantIsMuted req = new TdApi.ToggleGroupCallParticipantIsMuted();
        req.groupCallId = groupCallId;
        req.participantId = participantId;
        req.isMuted = isMuted;
        return send(req);
    }

    /**
     * Set the volume for a participant (0–20000, default 10000).
     * Note: class is {@code SetGroupCallParticipantVolumeLevel}.
     */
    public Mono<TdApi.Ok> setGroupCallParticipantVolumeLevel(int groupCallId,
                                                               TdApi.MessageSender participantId,
                                                               int volumeLevel) {
        TdApi.SetGroupCallParticipantVolumeLevel req = new TdApi.SetGroupCallParticipantVolumeLevel();
        req.groupCallId = groupCallId;
        req.participantId = participantId;
        req.volumeLevel = volumeLevel;
        return send(req);
    }

    /**
     * Invite a single user to a group call.
     * To invite multiple users, call this method in a loop or use {@code Flux.merge}.
     *
     * @param isVideo whether to invite for video (vs audio only)
     */
    public Mono<TdApi.InviteGroupCallParticipantResult> inviteGroupCallParticipant(int groupCallId,
                                                                                    long userId,
                                                                                    boolean isVideo) {
        TdApi.InviteGroupCallParticipant req = new TdApi.InviteGroupCallParticipant();
        req.groupCallId = groupCallId;
        req.userId = userId;
        req.isVideo = isVideo;
        return send(req);
    }

    /** Load more participants from the server into the local cache. */
    public Mono<TdApi.Ok> loadGroupCallParticipants(int groupCallId, int limit) {
        TdApi.LoadGroupCallParticipants req = new TdApi.LoadGroupCallParticipants();
        req.groupCallId = groupCallId;
        req.limit = limit;
        return send(req);
    }

    /** Get the list of senders that can be chosen as the video-chat participant for a chat. */
    public Mono<TdApi.MessageSenders> getVideoChatAvailableParticipants(long chatId) {
        TdApi.GetVideoChatAvailableParticipants req = new TdApi.GetVideoChatAvailableParticipants();
        req.chatId = chatId;
        return send(req);
    }

    /** Set the default participant (sender) for this account in a video chat. */
    public Mono<TdApi.Ok> setVideoChatDefaultParticipant(long chatId,
                                                           TdApi.MessageSender defaultParticipantId) {
        TdApi.SetVideoChatDefaultParticipant req = new TdApi.SetVideoChatDefaultParticipant();
        req.chatId = chatId;
        req.defaultParticipantId = defaultParticipantId;
        return send(req);
    }

    /** Toggle whether the local user's video is enabled in a group call. */
    public Mono<TdApi.Ok> toggleGroupCallIsMyVideoEnabled(int groupCallId, boolean isMyVideoEnabled) {
        TdApi.ToggleGroupCallIsMyVideoEnabled req = new TdApi.ToggleGroupCallIsMyVideoEnabled();
        req.groupCallId = groupCallId;
        req.isMyVideoEnabled = isMyVideoEnabled;
        return send(req);
    }

    /** Toggle whether the local user's video is paused in a group call. */
    public Mono<TdApi.Ok> toggleGroupCallIsMyVideoPaused(int groupCallId, boolean isMyVideoPaused) {
        TdApi.ToggleGroupCallIsMyVideoPaused req = new TdApi.ToggleGroupCallIsMyVideoPaused();
        req.groupCallId = groupCallId;
        req.isMyVideoPaused = isMyVideoPaused;
        return send(req);
    }

    /**
     * Set the speaking state of the local user in a group call.
     * Returns the {@link TdApi.MessageSender} that is identified as the active speaker.
     *
     * @param audioSource audio source id (SSRC)
     */
    public Mono<TdApi.MessageSender> setGroupCallParticipantIsSpeaking(int groupCallId,
                                                                         int audioSource,
                                                                         boolean isSpeaking) {
        TdApi.SetGroupCallParticipantIsSpeaking req = new TdApi.SetGroupCallParticipantIsSpeaking();
        req.groupCallId = groupCallId;
        req.audioSource = audioSource;
        req.isSpeaking = isSpeaking;
        return send(req);
    }
}
