package com.bignerdranch.android.networkingarchitecture.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bignerdranch.android.networkingarchitecture.controller.VenueDetailActivity;
import com.bignerdranch.android.networkingarchitecture.model.Venue;

import wiremock.org.apache.commons.lang.StringUtils;
import wiremock.org.mortbay.util.StringUtil;

public class VenueHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private VenueView mVenueView;
    private Venue mVenue;

    public VenueHolder(View itemView) {
        super(itemView);

        mVenueView = (VenueView) itemView;
        mVenueView.setOnClickListener(this);
    }

    public void bindVenue(Venue venue) {
        mVenue = venue;
        mVenueView.setVenueTitle(mVenue.getName());
        mVenueView.setVenueAddress(mVenue.getFormattedAddress());
        if(!StringUtils.isEmpty(mVenue.getIconUrl())) {
            mVenueView.setVenueIcon(mVenue.getIconUrl());
        }
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        Intent intent = VenueDetailActivity.newIntent(context, mVenue.getId());
        context.startActivity(intent);
    }
}

