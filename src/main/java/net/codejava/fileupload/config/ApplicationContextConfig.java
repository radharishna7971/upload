package net.codejava.fileupload.config;

import java.util.Properties;

import javax.sql.DataSource;

import net.codejava.fileupload.model.Associate;
import net.codejava.fileupload.model.AssociateTalentAssociateTypeMapping;
import net.codejava.fileupload.model.AssociateType;
import net.codejava.fileupload.model.Awards;
import net.codejava.fileupload.model.Company;
import net.codejava.fileupload.model.CreditTalentRoleMapping;
import net.codejava.fileupload.model.Credits;
import net.codejava.fileupload.model.Ethnicity;
import net.codejava.fileupload.model.Genres;
import net.codejava.fileupload.model.Keywords;
import net.codejava.fileupload.model.Role;
import net.codejava.fileupload.model.Talent;
import net.codejava.fileupload.model.TalentAwardCreditMapping;
import net.codejava.fileupload.model.UploadActivity;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan("net.codejava.fileupload")
@EnableTransactionManagement
public class ApplicationContextConfig {
    @Bean(name = "viewResolver")
    public InternalResourceViewResolver getViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }
    @Bean
    public AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter()
    {
        final AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter = new AnnotationMethodHandlerAdapter();
        final MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter = new MappingJackson2HttpMessageConverter();

        HttpMessageConverter<?>[] httpMessageConverter = { mappingJacksonHttpMessageConverter };

        String[] supportedHttpMethods = { "POST", "GET", "HEAD" };

        annotationMethodHandlerAdapter.setMessageConverters(httpMessageConverter);
        annotationMethodHandlerAdapter.setSupportedMethods(supportedHttpMethods);

        return annotationMethodHandlerAdapter;
    }
    
    @Bean(name = "dataSource")
    public DataSource getDataSource() {
    	BasicDataSource dataSource = new BasicDataSource();
    	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    	//dataSource.setUrl("jdbc:mysql://localhost:3306/smsstudios_db");
    	//dataSource.setUsername("root");
    	//dataSource.setPassword("gravitant");
    	//dataSource.setUrl("jdbc:mysql://127.7.195.2:3306/sms");
    	//dataSource.setUsername("adminymCkwsc");
    	//dataSource.setPassword("icX_iiSUZkjk");
    	dataSource.setUrl("jdbc:mysql://162.243.143.236:3306/smstudios_db");
    	dataSource.setUsername("ben");
    	dataSource.setPassword("smstudios2");
    	dataSource.setMaxTotal(30);
    	dataSource.setMaxIdle(10);
    	dataSource.setDefaultAutoCommit(true);
    	//dataSource.setMaxWaitMillis(20000);
    	
    	return dataSource;
    }
    
    
    private Properties getHibernateProperties() {
    	Properties properties = new Properties();
    	//properties.put("hibernate.show_sql", "true");
    	properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    	return properties;
    }
    
    @Autowired
    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(DataSource dataSource) {
    	LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);
    	sessionBuilder.addProperties(getHibernateProperties());
    	sessionBuilder.addAnnotatedClasses(Associate.class);
    	sessionBuilder.addAnnotatedClasses(AssociateTalentAssociateTypeMapping.class);
    	sessionBuilder.addAnnotatedClasses(AssociateType.class);
    	sessionBuilder.addAnnotatedClasses(Awards.class);
    	sessionBuilder.addAnnotatedClasses(Credits.class);
    	sessionBuilder.addAnnotatedClasses(CreditTalentRoleMapping.class);
    	sessionBuilder.addAnnotatedClasses(Genres.class);
    	sessionBuilder.addAnnotatedClasses(Keywords.class);
    	sessionBuilder.addAnnotatedClasses(Role.class);
    	sessionBuilder.addAnnotatedClasses(Talent.class);
    	sessionBuilder.addAnnotatedClasses(TalentAwardCreditMapping.class);
    	sessionBuilder.addAnnotatedClasses(Ethnicity.class);
    	sessionBuilder.addAnnotatedClasses(Company.class);
    	sessionBuilder.addAnnotatedClasses(UploadActivity.class);
    	return sessionBuilder.buildSessionFactory();
    }
    
	@Autowired
	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(
			SessionFactory sessionFactory) {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(
				sessionFactory);

		return transactionManager;
	}
    
    
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getCommonsMultipartResolver() {
    	CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    	multipartResolver.setMaxUploadSize(20971520); // 20MB
    	multipartResolver.setMaxInMemorySize(1048576);	// 1MB
    	return multipartResolver;
    }
}
