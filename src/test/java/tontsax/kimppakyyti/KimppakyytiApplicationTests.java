package tontsax.kimppakyyti;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import tontsax.kimppakyyti.dao.AccountDao;
import tontsax.kimppakyyti.dao.RideDao;
import tontsax.kimppakyyti.logic.Account;
import tontsax.kimppakyyti.logic.Ride;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class KimppakyytiApplicationTests {
	
	private static boolean databasePopulated = false;

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RideDao rideRepository;
	
	@Autowired
	private AccountDao accountRepository;
	
	@BeforeEach
	public void populateDatabase() {
		if(!databasePopulated) {
			Account account1 = new Account();
			Account account2 = new Account();
			
			account1.setNickName("Decimus");
			account2.setNickName("Tilemar");
			
			accountRepository.save(account1);
			accountRepository.save(account2);
			
			Ride ride1 = new Ride("Turku", "Helsinki", 10.0);
			Ride ride2 = new Ride("Turku", "Tampere", 23.5);
			
//			ride1.setDriver(account1);
//			ride2.setDriver(account2);
				
			System.out.println("Account 1 id: " + accountRepository.save(account1).getId());
			System.out.println("Account 2 id: " + accountRepository.save(account2).getId());
			System.out.println("Ride 1 id: " + rideRepository.save(ride1).getId());
			System.out.println("Ride 2 id: " + rideRepository.save(ride2).getId());
			
			databasePopulated = true;
		}
	}
	
//	@Test
//	public void registerToApp() throws Exception {
//		
//	}
//	
//	@Test
//	public void loginToAccount() throws Exception {
//		
//	}
	
	@Test
	@Order(1)
	public void checkConnection() throws Exception {
		mockMvc.perform(get("/rides"))
				.andExpect(status().isOk());
	}
	
	@Test
	@Order(2)
	public void getListOfAllRides() throws Exception {
		checkRidesListLength(2);
	}
	
	@Test
	@Order(3)
	public void getRidesByOrigin() throws Exception {
		performRequestAndExpectJson(get("/rides/from{origin}", "Turku"))
		.andExpect(jsonPath("$.length()", is(2)));
	}
	
	@Test
	@Order(4)
	public void getRidesByDestination() throws Exception {
		performRequestAndExpectJson(get("/rides/to{destination}", "Helsinki"))
		.andExpect(jsonPath("$.length()", is(1)));
	}
	
	@Test
	@Order(5)
	public void findRideById() throws Exception {
		getRideById((long) 3, "Turku", "Helsinki");
		getRideById((long) 4, "Turku", "Tampere");
	}
	
	private void getRideById(Long id, String origin, String destination) throws Exception {
		performRequestAndExpectJson(get("/rides/{id}", id))
			.andExpect(jsonPath("$.id").value(id.toString()))	
			.andExpect(jsonPath("$.origin").value(origin))
			.andExpect(jsonPath("$.destination").value(destination));
	}
	
	@Test
	@Order(6)
	public void postRide() throws Exception {
		String jsonRide = "{\"origin\":\"Tampere\",\"destination\":\"Oulu\",\"price\":\"25.0\"}";
//		String jsonRide = "{\"origin\":\"Tampere\",\"destination\":\"Oulu\",\"price\":\"25.0\", "
//				+ "\"driver\":\"0\""
//				+ "\"passangers:\"null\"\""
//				+ "}";
		performRequestAndExpectJson(post("/rides")
			.contentType(MediaType.APPLICATION_JSON)
//			.contentType("application/json")
			.content(jsonRide))
			.andExpect(jsonPath("$.origin").value("Tampere"))
			.andExpect(jsonPath("$.destination").value("Oulu"));
		
		checkRidesListLength(3);
	}
	
	@Test
	@Order(7)
	public void deleteRideById() throws Exception {
		MvcResult request = performRequestAndExpectJson(delete("/rides/{id}", 4L)).andReturn();
		String content = request.getResponse().getContentAsString();
		
		Assert.assertTrue(content.equalsIgnoreCase("true"));
		checkRidesListLength(2);
	}
	
	@Test
	@Order(8)
	public void updateRide() throws Exception {
		String jsonRide = "{\"origin\":\"Turku\",\"destination\":\"Oulu\",\"price\":\"25.0\"}";
		performRequestAndExpectJson(put("/rides/{id}", 3L)
				.contentType(MediaType.APPLICATION_JSON)
//				.contentType("application/json")
				.content(jsonRide))
			.andExpect(jsonPath("$.id").value("3"))
			.andExpect(jsonPath("$.origin").value("Turku"))
			.andExpect(jsonPath("$.destination").value("Oulu"))
			.andExpect(jsonPath("$.price").value("25.0"));
	}
	
	private ResultActions performRequestAndExpectJson(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request)
				.andDo(print())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
//				.andExpect(content().contentType("application/json"));
	}
	
	private void checkRidesListLength(int length) throws Exception {
		performRequestAndExpectJson(get("/rides"))
		.andExpect(jsonPath("$.length()", is(length)));
	}
}
