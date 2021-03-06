package cn.songm.sso.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import cn.songm.sso.dao.SessionDao;
import cn.songm.sso.entity.Session;
import cn.songm.sso.redis.SessionRedis;

public class SessionDaoAspect {

    @Autowired
    private SessionRedis sessionRedis;
    @Autowired
    private SessionDao sessionDao;
    
    public Session selectOneById(ProceedingJoinPoint point) throws Throwable {
        Session result = sessionRedis.selectById((String)point.getArgs()[0]);
        // 缓存中存在数据，直接返回
        if (result != null) return result;
        // 否则查询数据库
        result = (Session) point.proceed();
        // 数据库中查询的数据同步到缓存
        if (result != null) {
            sessionRedis.insert(result);
        }
        return result;
    }
    
    public void updateAccess(String sesId) {
        Session s = sessionDao.selectOneById(sesId);
        // 修改缓存
        sessionRedis.updateAccess(sesId, s);
    }
    
    public void updateUserId(String sesId) throws Throwable {
        Session s = sessionDao.selectOneById(sesId);
        // 修改缓存
        sessionRedis.updateUserId(sesId, s);
    }
}
