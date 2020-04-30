
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class MainTest{


	public static void main(String[] args) throws Exception
	{
		System.out.println("démarrage du bot");
		String token = args[0];				  
		new JDABuilder(token).addEventListeners(new MyListener()).build();
	}
}
