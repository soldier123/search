package processor;

import annotations.Process;
import play.Logger;
import play.Play;
import play.modules.guice.GuicePlugin;
import play.modules.guice.InjectSupport;

import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午2:45
 * 功能描述:
 */
public class DefaultProcessorFactory extends AbstractProcessorFactory {
    private static DefaultProcessorFactory instance = new DefaultProcessorFactory();

    public static AbstractProcessorFactory getInstance() {
        return instance;
    }

    //装载processor
    private DefaultProcessorFactory() {
        configure();
        Logger.debug("请求处理器 processor配置完毕");
    }

    @Override
    protected void configure() {
        List<Class> list = Play.classloader.getAnnotatedClasses(Process.class);
        GuicePlugin guicePlugin = Play.plugin(GuicePlugin.class);
        for (Class clazz : list) {
            //检查当前proccessor是否设置了guice注入,如果设置了则从guice中获取process对像  否则反射
            Process tag = (Process) clazz.getAnnotation(annotations.Process.class);
            String classKey = "".equals(tag.name()) ? clazz.getSimpleName() : tag.name();
            if (clazz.isAnnotationPresent(InjectSupport.class)) {
                //从guice容器中取
                   processors.put(classKey, (Processor) guicePlugin.getBeanOfType(clazz));
                Logger.info("从guice中取得:%s", guicePlugin.getBeanOfType(clazz));
            } else {
                try {
                    processors.put(classKey, (Processor) clazz.newInstance());
                } catch (Exception e) {
                    Logger.error("初始化:" + clazz + "为processor.Prcessor时出错，请检查类型", e);
                }
            }
        }
    }
}
