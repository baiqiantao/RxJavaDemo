package rx.test.bqt.com.rxjavademo.rx;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubApi {
	
	@GET("repos/{owner}/{repo}/contributors")
	Observable<List<Contributor>> contributors(@Path("owner") String owner, @Path("repo") String repo);
	
	@GET("repos/{owner}/{repo}/contributors")
	List<Contributor> getContributors(@Path("owner") String owner, @Path("repo") String repo);
	
	@GET("users/{user}")
	Observable<User> user(@Path("user") String user);
	
	@GET("users/{user}")
	User getUser(@Path("user") String user);
}

class Contributor {
	public String login;
	public long contributions;
	
	public Contributor() {
	}
	
	public Contributor(String login, long contributions) {
		this.login = login;
		this.contributions = contributions;
	}
}

class User {
	public String name;
	public String email;
}
