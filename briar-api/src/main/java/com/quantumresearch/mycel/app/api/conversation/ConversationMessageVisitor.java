package com.quantumresearch.mycel.app.api.conversation;

import com.quantumresearch.mycel.app.api.blog.BlogInvitationRequest;
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse;
import com.quantumresearch.mycel.app.api.forum.ForumInvitationRequest;
import com.quantumresearch.mycel.app.api.forum.ForumInvitationResponse;
import com.quantumresearch.mycel.app.api.introduction.IntroductionRequest;
import com.quantumresearch.mycel.app.api.introduction.IntroductionResponse;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageHeader;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationRequest;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConversationMessageVisitor<T> {

	T visitPrivateMessageHeader(PrivateMessageHeader h);

	T visitBlogInvitationRequest(BlogInvitationRequest r);

	T visitBlogInvitationResponse(BlogInvitationResponse r);

	T visitForumInvitationRequest(ForumInvitationRequest r);

	T visitForumInvitationResponse(ForumInvitationResponse r);

	T visitGroupInvitationRequest(GroupInvitationRequest r);

	T visitGroupInvitationResponse(GroupInvitationResponse r);

	T visitIntroductionRequest(IntroductionRequest r);

	T visitIntroductionResponse(IntroductionResponse r);
}
