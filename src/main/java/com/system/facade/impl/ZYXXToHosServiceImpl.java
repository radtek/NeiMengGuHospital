package com.system.facade.impl;

import com.system.controller.util.ExceptionHandlerController;
import com.system.dao.DB2.test1.PtsVwCyxxDao;
import com.system.dao.DB2.test1.PtsVwZyxxDao;
import com.system.entity.*;
import com.system.entity.DB2.test1.PtsVwCyxx;
import com.system.entity.DB2.test1.PtsVwZyxx;
import com.system.facade.ZYXXToHosService;
import com.system.pojo.CreateSysAreaInfo;
import com.system.pojo.CreateSysMedicalInsuranceInfo;
import com.system.pojo.CreateSysNursingLevelInfo;
import com.system.service.SysAreaService;
import com.system.service.SysHospitalizationService;
import com.system.service.SysMedicalInsuranceService;
import com.system.service.SysNursingLevelService;
import com.system.util.database.DataSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: 李景然
 * @Date: 2018/7/2 09:30
 * @Description:
 */
@Service("zyxxAndCYXXToHosService")
public class ZYXXToHosServiceImpl implements ZYXXToHosService {
    private Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);
    @Resource
    private SysHospitalizationService sysHospitalizationService;

    @Resource
    private PtsVwZyxxDao ptsVwZyxxDao;

    @Resource
    private PtsVwCyxxDao ptsVwCyxxDao;

    @Resource
    private SysAreaService sysAreaService;

    @Resource
    private SysNursingLevelService sysNursingLevelService;

    @Resource
    private SysMedicalInsuranceService sysMedicalInsuranceService;

    @Override
    @DataSwitch(dataSource = "dataSource3")
    public List<PtsVwZyxx> getZYXXList(Date startTime, Date endTime) {
        List<PtsVwZyxx> list = ptsVwZyxxDao.selectAll().stream().filter(item -> (item.getRyrq().after(startTime) && item.getRyrq().before(endTime))||(item.getRyrq().equals(startTime))||(item.getRyrq().equals(endTime))).collect(Collectors.toList());
        return list;
    }

    @Override
    @DataSwitch(dataSource = "dataSource3")
    public List<PtsVwZyxx> getZYXXList() {
        List<PtsVwZyxx> list = ptsVwZyxxDao.selectAll();
        return list;
    }

    @DataSwitch(dataSource = "dataSource3")
    private PtsVwZyxx getZYXX(String zyh,int times){
        List<PtsVwZyxx> list=ptsVwZyxxDao.selectByExample(getZYXXExample(times,zyh));
        if(list!=null&&list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    @DataSwitch(dataSource = "dataSource3")
    public List<PtsVwCyxx> getCYXXList(Date startTime, Date endTime) {
        List<PtsVwCyxx> list = ptsVwCyxxDao.selectByExample(getExample(startTime,endTime));
        return list;
    }

    private Example getExample(Date startTime, Date endTime) {
        Example example = new Example(PtsVwCyxx.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andBetween("cyrq",startTime,endTime);
        return example;
    }

    @Override
    @DataSwitch(dataSource = "dataSource3")
    public PtsVwCyxx getCYXX(String zyh,int times){
        List<PtsVwCyxx> list=ptsVwCyxxDao.selectByExample(getCYXXExample(times,zyh));
        if(list!=null&&list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    @DataSwitch(dataSource = "dataSource1")
    public boolean insertHos(PtsVwZyxx ptsVwZyxx) {
        SysHospitalization sysHospitalization = convertToSysHospitalization(ptsVwZyxx,1,"未探访");
        return sysHospitalizationService.insert(sysHospitalization);
    }

    @Override
    @DataSwitch(dataSource = "dataSource1")
    public boolean update(PtsVwZyxx ptsVwZyxx,SysHospitalization old){
        SysHospitalization sysHospitalization = convertToSysHospitalization(ptsVwZyxx,old.getEscortsNum(),old.getVisitStatus());
        sysHospitalization.setId(old.getId());
        return sysHospitalizationService.update(sysHospitalization);
    }

    @DataSwitch(dataSource = "dataSource3")
    public boolean isExitHos(SysHospitalization item) {
        PtsVwZyxx zyxx=getZYXX(item.gethId(),item.gethTimes());
        if (zyxx!=null)
            return false;
        return true;
    }

    @DataSwitch(dataSource = "dataSource1")
    public boolean deleteExitHos(List<Long> idList){
        return sysHospitalizationService.delete(idList);
    }

    private SysHospitalization convertToSysHospitalization(PtsVwZyxx ptsVwZyxx,int escortsNum,String visitStatus) {
        if (null == ptsVwZyxx) {
            return null;
        }
        SysHospitalization result = new SysHospitalization();
        result.sethDate(ptsVwZyxx.getRyrq());
        result.setDcrName(ptsVwZyxx.getZzys());
        result.setpAge(ptsVwZyxx.getNl());
        result.sethId(ptsVwZyxx.getZyh());
        result.setpName(ptsVwZyxx.getXm());
        result.setpSex(ptsVwZyxx.getXb());
        result.sethBed(ptsVwZyxx.getRybch());

        //住院次数
        result.sethTimes(ptsVwZyxx.getZycs());
        //陪人数
        result.setEscortsNum(escortsNum);
        //探访状态
        result.setVisitStatus(visitStatus);

        //处理医保类型
        SysMedicalInsurance sysMedicalInsurance = sysMedicalInsuranceService.get(ptsVwZyxx.getYlfkfs());
        if (sysMedicalInsurance == null) {
            CreateSysMedicalInsuranceInfo createSysMedicalInsuranceInfo = new CreateSysMedicalInsuranceInfo();
            createSysMedicalInsuranceInfo.setValue(ptsVwZyxx.getYlfkfs());
            sysMedicalInsuranceService.insert(createSysMedicalInsuranceInfo);
            sysMedicalInsurance = sysMedicalInsuranceService.get(ptsVwZyxx.getYlfkfs());
        }
        result.setpInsur(sysMedicalInsurance.getId());

        //处理病区
        SysArea sysArea = sysAreaService.get(ptsVwZyxx.getRyks());
        if (sysArea == null) {
            CreateSysAreaInfo createSysAreaInfo = new CreateSysAreaInfo();
            createSysAreaInfo.setValue(ptsVwZyxx.getRyks());
            sysAreaService.insert(createSysAreaInfo);
            sysArea = sysAreaService.get(ptsVwZyxx.getRyks());
        }
        result.sethArea(sysArea.getId());

        //处理护理
        SysNursingLevel sysNursingLevel = sysNursingLevelService.get(ptsVwZyxx.getHljb());
        if (sysNursingLevel == null) {
            CreateSysNursingLevelInfo createSysNursingLevelInfo = new CreateSysNursingLevelInfo();
            createSysNursingLevelInfo.setValue(ptsVwZyxx.getHljb());
            sysNursingLevelService.insert(createSysNursingLevelInfo);
            sysNursingLevel = sysNursingLevelService.get(ptsVwZyxx.getHljb());
        }
        result.setNursingLevel(sysNursingLevel.getId());

        //待确认,先统一设置成“住院”了
        result.setpStatus(1);

        return result;
    }

    private Example getCYXXExample(int times, String zyh) {
        Example example = new Example(PtsVwCyxx.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("zyh", zyh);
        criteria.andEqualTo("zycs", times);

        return example;
    }

    private Example getZYXXExample(int times, String zyh) {
        Example example = new Example(PtsVwZyxx.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("zyh", zyh);
        criteria.andEqualTo("zycs", times);

        return example;
    }

}