package tontsax.kimppakyyti.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import tontsax.kimppakyyti.logic.Account;

public interface AccountDao extends JpaRepository<Account, Long> {

}
