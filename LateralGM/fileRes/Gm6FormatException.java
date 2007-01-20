package fileRes;

public class Gm6FormatException extends Exception
	{
	private static final long serialVersionUID = 1L;

	public Gm6FormatException(String message)
		{
		super(message);
		}

	public String stackAsString()
		{
		StackTraceElement[] els = getStackTrace();
		String res = "";
		for (int i = 0; i < els.length; i++)
			{
			res += els[i].toString();
			if (i != els.length - 1) res += "\n";
			}
		return res;
		}
	}