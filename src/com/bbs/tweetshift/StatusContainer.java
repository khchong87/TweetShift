package com.bbs.tweetshift;

import winterwell.jtwitter.Status;

public class StatusContainer {
	Status status;
	String profileImageURL;
	String source;
	String userName;
	

	public StatusContainer(Status status, String url, String source, String username) {
		this.status = status;
		this.profileImageURL = url;
		this.source = source;
		this.userName = username;
	}

	public Status getStatus() {
		return status;
	}

	public String getProfileImageURL() {
		return profileImageURL;
	}

	public String getSource() {
		return source;
	}

	public String getUserName() {
		return userName;
	}
}
