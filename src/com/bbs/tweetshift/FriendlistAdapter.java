package com.bbs.tweetshift;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendlistAdapter extends ArrayAdapter<UserContainer> {
	TwitterApplication yamba;
	List<UserContainer> userList;
	private ImageLoader imageLoader;
	Context context;

	public FriendlistAdapter(Context context, int textViewResourceId,
			List<UserContainer> userList) {
		super(context, R.layout.row_friend, R.id.textViewScreenName2, userList);
		this.context = context;
		this.userList = userList;
		imageLoader = new ImageLoader(context);
	}

	@Override
	public int getCount() {
		if (userList != null)
			return userList.size();
		else
			return 0;
	}

	@Override
	public UserContainer getItem(int arg0) {
		return userList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return R.id.textViewName2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_friend, null);
		}
		UserContainer user = userList.get(position);
		if (null != user) {
			TextView username = (TextView) v.findViewById(R.id.textViewName2);
			TextView screenName = (TextView) v.findViewById(R.id.textViewScreenName2);
			ImageView profilePicture = (ImageView) v
					.findViewById(R.id.imageViewFriendList);
			if (null != username) {
				username.setText(user.getName());
			}
			if (null != screenName) {
				screenName.setText("@"+user.getScreenName());
			}
			if (null != profilePicture) {
				imageLoader.DisplayImage(user.getProfilePictureURL().toString().replace("_normal", "_reasonably_small"),
						profilePicture);
			}
		}
		return super.getView(position, v, parent);
	}

	public void setUserList(List<UserContainer> userList) {
		this.userList = userList;
		// this.notifyDataSetChanged();
	}
}
