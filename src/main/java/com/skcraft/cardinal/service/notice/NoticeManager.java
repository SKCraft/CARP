package com.skcraft.cardinal.service.notice;

import com.google.common.util.concurrent.ListenableFuture;

public interface NoticeManager {

    ListenableFuture<Notice> getNext(String group);

}
