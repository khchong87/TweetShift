package com.bbs.tweetshift;

import java.net.URI;
import java.util.Date;

import winterwell.jtwitter.User;

public class UserContainer {
	private User user;

	private Long userID;
	private String name;
	private String screenName;
	private Date createdAt;
	private String description;
	private Integer favouriteCount;
	private Integer followerCount;
	private URI profilePictureURL;
	private String profileBackgroundColor;
	private URI profileBackgroundImageURL;
	private String profileBackgroundTile;
	private Integer statusCount;
	private Integer friendCount;

	public UserContainer(User user) {
		this.user = user;
		this.name = user.getName();
		this.userID = user.getId();
		this.screenName = user.getScreenName();
		this.createdAt = user.getCreatedAt();
		this.description = user.getDescription();
		this.favouriteCount = user.getFavoritesCount();
		this.followerCount = user.getFollowersCount();
		this.profilePictureURL = user.getProfileImageUrl();
		this.friendCount = user.friendsCount;
		this.profileBackgroundImageURL = user.profileBackgroundImageUrl;
	}
	
	public UserContainer() {
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getFavouriteCount() {
		return favouriteCount;
	}

	public void setFavouriteCount(Integer favouriteCount) {
		this.favouriteCount = favouriteCount;
	}

	public Integer getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(Integer followerCount) {
		this.followerCount = followerCount;
	}

	public URI getProfilePictureURL() {
		return profilePictureURL;
	}

	public void setProfilePictureURL(URI profilePictureURL) {
		this.profilePictureURL = profilePictureURL;
	}

	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	public void setProfileBackgroundColor(String profileBackgroundColor) {
		this.profileBackgroundColor = profileBackgroundColor;
	}

	public URI getProfileBackgroundImageURL() {
		return profileBackgroundImageURL;
	}

	public void setProfileBackgroundImageURL(URI profileBackgroundImageURL) {
		this.profileBackgroundImageURL = profileBackgroundImageURL;
	}

	public String getProfileBackgroundTile() {
		return profileBackgroundTile;
	}

	public void setProfileBackgroundTile(String profileBackgroundTile) {
		this.profileBackgroundTile = profileBackgroundTile;
	}

	public Integer getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(Integer statusCount) {
		this.statusCount = statusCount;
	}

	public Integer getFriendCount() {
		return friendCount;
	}

	public void setFriendCount(Integer friendCount) {
		this.friendCount = friendCount;
	}
	
	@Override
	public String toString() {
		return "@"+screenName;
	}

}
