package processor;

import java.util.HashMap;
import java.util.Map;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午2:41
 * 功能描述:
 */
public abstract class AbstractProcessorFactory {

  protected static Map<String,Processor> processors = new HashMap<String, Processor>();
  public  static AbstractProcessorFactory buildDefaultProcessorFacoty(){
      return DefaultProcessorFactory.getInstance();
  }
  public Processor lookup(String cmdName){
      return processors.get(cmdName);
  }
  protected abstract void configure();

}
