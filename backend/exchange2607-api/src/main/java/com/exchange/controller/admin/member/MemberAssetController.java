package com.exchange.controller.admin.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.member.param.MemberAssetAdjustParam;
import com.exchange.member.param.MemberAssetOpenAccountParam;
import com.exchange.member.param.MemberAssetQueryCondParam;
import com.exchange.member.service.MemberAssetService;
import com.exchange.member.vo.MemberAssetSubtotalVO;
import com.exchange.member.vo.MemberAssetSummaryVO;
import com.exchange.member.vo.MemberAssetVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/memberAsset")
public class MemberAssetController {

	@Autowired
	private MemberAssetService memberAssetService;

	@PostMapping("/findByPage")
	public Result<PageResult<MemberAssetVO>> findByPage(MemberAssetQueryCondParam param) {
		return Result.success(memberAssetService.findByPage(param));
	}

	@PostMapping("/subtotal")
	public Result<MemberAssetSubtotalVO> subtotal(MemberAssetQueryCondParam param) {
		return Result.success(memberAssetService.subtotal(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员资产, operate = "增加可用余额")
	@SaCheckPermission("memberAsset.adjustAdd")
	@PostMapping("/addAvailableBalance")
	public Result<String> addAvailableBalance(MemberAssetAdjustParam param) {
		memberAssetService.addAvailableBalance(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员资产, operate = "减少可用余额")
	@SaCheckPermission("memberAsset.adjustReduce")
	@PostMapping("/reduceAvailableBalance")
	public Result<String> reduceAvailableBalance( MemberAssetAdjustParam param) {
		memberAssetService.reduceAvailableBalance(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员资产, operate = "冻结资金")
	@SaCheckPermission("memberAsset.freeze")
	@PostMapping("/freezeBalance")
	public Result<String> freezeBalance( MemberAssetAdjustParam param) {
		memberAssetService.freezeBalance(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员资产, operate = "解冻资金")
	@SaCheckPermission("memberAsset.unfreeze")
	@PostMapping("/unfreezeBalance")
	public Result<String> unfreezeBalance( MemberAssetAdjustParam param) {
		memberAssetService.unfreezeBalance(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员资产, operate = "开通资产账户")
	@SaCheckPermission("memberAsset.openAccount")
	@PostMapping("/openAssetAccount")
	public Result<String> openAssetAccount( MemberAssetOpenAccountParam param) {
		memberAssetService.openAssetAccount(param);
		return Result.success();
	}

	@PostMapping("/getMemberAssetSummary")
	public Result<MemberAssetSummaryVO> getMemberAssetSummary(String memberAccountId, String fiatCode) {
		MemberAssetSummaryVO vo = new MemberAssetSummaryVO();
		// 后台可按传入法币折算；未传或非法时由 getTotalValue 内部回落 CNY
		vo.setTotalValue(memberAssetService.getTotalValue(memberAccountId, fiatCode));
		vo.setByAccountType(memberAssetService.findAssetsByAccountTypeSummary(memberAccountId));
		vo.setByCurrency(memberAssetService.findAssetsByCurrencySummary(memberAccountId));
		return Result.success(vo);
	}

}
