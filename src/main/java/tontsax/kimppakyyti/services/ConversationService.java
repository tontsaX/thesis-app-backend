package tontsax.kimppakyyti.services;

import java.util.List;

import tontsax.kimppakyyti.dao.Account;
import tontsax.kimppakyyti.dao.Conversation;
import tontsax.kimppakyyti.dao.Message;

public interface ConversationService {
	public abstract Conversation getConversation(Long id);
	public abstract Conversation getConversation(Long id, List<Account> owners);
	
	public abstract Conversation save(Conversation conversation);
	public abstract Message save(Message message);
	public abstract Message save(Conversation conversation, Message message);
}
