package config;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import dao.NewsDao;
import dao.NewsDaoImpl;
import play.modules.guice.GuiceSupport;
import service.DefaultNewsServiceImpl;
import service.NewsService;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:52
 * 功能描述:
 * 配置IOC注入关系
 */
public class GuiceConfig  extends GuiceSupport {

    @Override
    protected Injector configure() {

        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NewsService.class).to(DefaultNewsServiceImpl.class).in(Singleton.class);
                bind(NewsDao.class).to(NewsDaoImpl.class).in(Singleton.class);
            }
        });
    }
}
