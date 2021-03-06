package com.bitlove.fetlife.view.screen.resource.members;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bitlove.fetlife.R;
import com.bitlove.fetlife.event.ServiceCallFailedEvent;
import com.bitlove.fetlife.event.ServiceCallFinishedEvent;
import com.bitlove.fetlife.event.ServiceCallStartedEvent;
import com.bitlove.fetlife.model.pojos.fetlife.db.RelationReference;
import com.bitlove.fetlife.model.pojos.fetlife.dbjson.Member;
import com.bitlove.fetlife.model.service.FetLifeApiIntentService;
import com.bitlove.fetlife.view.screen.BaseActivity;
import com.bitlove.fetlife.view.screen.resource.ResourceActivity;
import com.bitlove.fetlife.view.screen.resource.profile.RelationsFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MembersActivity extends ResourceActivity {

    private ViewPager viewPager;

    public static void startActivity(BaseActivity baseActivity) {
        Intent intent = new Intent(baseActivity, MembersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        baseActivity.startActivity(intent);
    }

    @Override
    protected void onResourceCreate(Bundle savedInstanceState) {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                Member user = getFetLifeApplication().getUserSessionManager().getCurrentUser();
                if (user == null) {
                    return null;
                }
                String memberId = user.getId();
                switch (position) {
                    case 0:
                        return SearchMemberFragment.newInstance(null);
                    case 1:
                        return RelationsFragment.newInstance(memberId,RelationReference.VALUE_RELATIONTYPE_FRIEND);
                    case 2:
                        return RelationsFragment.newInstance(memberId,RelationReference.VALUE_RELATIONTYPE_FOLLOWING);
                    case 3:
                        return RelationsFragment.newInstance(memberId,RelationReference.VALUE_RELATIONTYPE_FOLLOWER);
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.title_fragment_member_serach);
                    case 1:
                        return getString(R.string.title_fragment_profile_friends);
                    case 2:
                        return getString(R.string.title_fragment_profile_following);
                    case 3:
                        return getString(R.string.title_fragment_profile_followers);
                    default:
                        return null;
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResourceListCallStarted(ServiceCallStartedEvent serviceCallStartedEvent) {
        if (isRelatedCall(serviceCallStartedEvent.getServiceCallAction(), serviceCallStartedEvent.getParams())) {
            showProgress();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void callFinished(ServiceCallFinishedEvent serviceCallFinishedEvent) {
        if (isRelatedCall(serviceCallFinishedEvent.getServiceCallAction(),serviceCallFinishedEvent.getParams()) && !isRelatedCall(FetLifeApiIntentService.getActionInProgress(),FetLifeApiIntentService.getInProgressActionParams())) {
            dismissProgress();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void callFailed(ServiceCallFailedEvent serviceCallFailedEvent) {
        if (isRelatedCall(serviceCallFailedEvent.getServiceCallAction(), serviceCallFailedEvent.getParams())) {
            dismissProgress();
        }
    }

    private boolean isRelatedCall(String serviceCallAction, String[] params) {
        Member currentUser = getFetLifeApplication().getUserSessionManager().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        if (params != null && params.length > 0 && currentUser.getId() != null && !currentUser.getId().equals(params[0])) {
            return false;
        }
        if (FetLifeApiIntentService.ACTION_APICALL_SEARCH_MEMBER.equals(serviceCallAction)) {
            return true;
        }
        if (FetLifeApiIntentService.ACTION_APICALL_MEMBER_RELATIONS.equals(serviceCallAction)) {
            return true;
        }
        if (FetLifeApiIntentService.ACTION_APICALL_MEMBER.equals(serviceCallAction)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResourceStart() {
    }

    @Override
    protected void onCreateActivityComponents() {
    }

    @Override
    protected void onSetContentView() {
        setContentView(R.layout.activity_members);
    }

}
