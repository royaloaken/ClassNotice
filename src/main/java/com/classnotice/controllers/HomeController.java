package com.classnotice;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.ModelMap;

import com.classnotice.services.NoticeService;
import com.classnotice.services.UserService;
import com.classnotice.db.entities.Notice;
import com.classnotice.db.entities.Student;
import com.classnotice.beans.ListItem;
import com.classnotice.beans.ReadStatus;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

@Controller
@SessionAttributes({"uid"})
public class HomeController{

	@Autowired
	private NoticeService noticeService;
	@Autowired
	private UserService userService;

	@ModelAttribute
	public void setPersonalInfo(@ModelAttribute("uid") String uid,ModelMap model){
		model.addAttribute("selfPortrait",userService.getPortraitUrl(uid));
		model.addAttribute("unreadCount",noticeService.countUnreadNotice(uid));
		model.addAttribute("readCount",noticeService.countReadNotice(uid));
		model.addAttribute("starCount",noticeService.countStarNotice(uid));
		model.addAttribute("sentCount",noticeService.countSentNotice(uid));
		model.addAttribute("isAdmin",userService.isAdmin(uid));
	}

	@RequestMapping(path="/",method=RequestMethod.GET)
	public String showUnreadList(@RequestParam(name="page",defaultValue="0",required=false) int page,@ModelAttribute("uid") String uid,ModelMap model){
		model.addAttribute("pageTitle","未读通知");
		model.addAttribute("pageIndex",0);
		//Assemble main list using "ListItem" bean
		List<Notice> unreadNotices=noticeService.getUnreadNotice(uid,page);
		model.addAttribute("listItems",produceListItems(unreadNotices,uid));

		return "noticeList";
	}

	@RequestMapping(path="/read",method=RequestMethod.GET)
	public String showReadList(@RequestParam(name="page",defaultValue="0",required=false) int page,@ModelAttribute("uid") String uid,ModelMap model){
		model.addAttribute("pageTitle","已读通知");
		model.addAttribute("pageIndex",1);
		//Assemble main list using "ListItem" bean
		List<Notice> readNotices=noticeService.getReadNotice(uid,page);
		model.addAttribute("listItems",produceListItems(readNotices,uid));

		return "noticeList";
	}

	@RequestMapping(path="/star",method=RequestMethod.GET)
	public String showStarList(@RequestParam(name="page",defaultValue="0",required=false) int page,@ModelAttribute("uid") String uid,ModelMap model){
		model.addAttribute("pageTitle","标星通知");
		model.addAttribute("pageIndex",2);
		//Assemble main list using "ListItem" bean
		List<Notice> starNotices=noticeService.getStarNotice(uid,page);
		model.addAttribute("listItems",produceListItems(starNotices,uid));

		return "noticeList";
	}

	@RequestMapping(path="/readStatus",method=RequestMethod.GET)
	public String showReadStatus(@RequestParam(name="page",defaultValue="0",required=false) int page,@ModelAttribute("uid") String uid,ModelMap model){
		List<Notice> sentNotices=noticeService.getSentNotice(uid,page);
		model.addAttribute("readStatuses",produceReadStatuses(sentNotices));
		return "readStatus";
	}

	@RequestMapping(path="/publishNotice",method=RequestMethod.GET)
	public String showPublishNoticePage(){
		return "newNotice";
	}

	@RequestMapping(path="/notice/{noticeId}",method=RequestMethod.GET)
	public String showNotice(@ModelAttribute("uid") String uid,@PathVariable int noticeId,ModelMap model){
		model.addAttribute("pageTitle","通知阅读");
		//Find the notice and set it up,then put it into model
		Notice notice=noticeService.getNotice(noticeId);
		model.addAttribute("noticeItem",convertToListItem(notice,uid));
		//Set read status to true
		noticeService.setRead(uid,noticeId,true);
		//Calculate page counts
		model.addAttribute("unreadCount",noticeService.countUnreadNotice(uid));
		model.addAttribute("readCount",noticeService.countReadNotice(uid));
		return "readPage";
	}

	//helper function
	private List<ListItem> produceListItems(List<Notice> notices,String uid){
		List<ListItem> listItems=new ArrayList<ListItem>();

		Iterator<Notice> noticeIterator=notices.iterator();
		while(noticeIterator.hasNext()){
			ListItem eachItem=new ListItem();

			Notice eachNotice=noticeIterator.next();
			eachItem.setNotice(eachNotice);
			eachItem.setSenderPortrait(userService.getPortraitUrl(eachNotice.getSender()));
			eachItem.setSenderBanner(userService.getStudent(eachNotice.getSender()));
			eachItem.setStar(noticeService.getStar(uid,eachNotice.getID()));

			listItems.add(eachItem);
		}
		return listItems;
	}

	private List<ReadStatus> produceReadStatuses(List<Notice> notices){
		List<ReadStatus> readStatuses=new ArrayList<ReadStatus>();
		Iterator<Notice> noticeIterator=notices.iterator();
	       	while(noticeIterator.hasNext()){	
			Notice notice=noticeIterator.next();

			ReadStatus newStatus=new ReadStatus();
			newStatus.setNotice(notice);
			newStatus.setReadCount(noticeService.countReadNotice(notice.getID()));
			newStatus.setNoticePath(noticeService.getNoticePath(notice.getID()));
			newStatus.setReceiversCount(noticeService.countReceivers(notice.getID()));
			readStatuses.add(newStatus);
		}
		return readStatuses;
	}
	//helper function
	private ListItem convertToListItem(Notice notice,String uid){
		ListItem item=new ListItem();
		item.setNotice(notice);
		item.setSenderBanner(userService.getStudent(notice.getSender()));
		item.setSenderPortrait(userService.getPortraitUrl(notice.getSender()));
		item.setStar(noticeService.getStar(uid,notice.getID()));
		return item;
	}
}
