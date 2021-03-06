/**
 * This file is part of mycollab-mobile.
 *
 * mycollab-mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-mobile.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.mobile.module.crm.view.campaign;

import com.mycollab.common.UrlEncodeDecoder;
import com.mycollab.common.i18n.GenericI18Enum;
import com.mycollab.eventmanager.EventBusFactory;
import com.mycollab.mobile.module.crm.events.CampaignEvent;
import com.mycollab.mobile.module.crm.view.AbstractCrmPresenter;
import com.mycollab.mobile.shell.events.ShellEvent;
import com.mycollab.module.crm.domain.CampaignWithBLOBs;
import com.mycollab.module.crm.domain.SimpleCampaign;
import com.mycollab.module.crm.i18n.CampaignI18nEnum;
import com.mycollab.module.crm.service.CampaignService;
import com.mycollab.security.RolePermissionCollections;
import com.mycollab.spring.AppContextUtil;
import com.mycollab.vaadin.MyCollabUI;
import com.mycollab.vaadin.UserUIContext;
import com.mycollab.vaadin.events.DefaultEditFormHandler;
import com.mycollab.vaadin.mvp.ScreenData;
import com.mycollab.vaadin.ui.NotificationUtil;
import com.vaadin.ui.HasComponents;

/**
 * @author MyCollab Ltd.
 * @since 4.1
 */
public class CampaignAddPresenter extends AbstractCrmPresenter<CampaignAddview> {
    private static final long serialVersionUID = 1L;

    public CampaignAddPresenter() {
        super(CampaignAddview.class);
    }

    @Override
    protected void postInitView() {
        view.getEditFormHandlers().addFormHandler(new DefaultEditFormHandler<SimpleCampaign>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSave(final SimpleCampaign campaign) {
                saveCampaign(campaign);
                EventBusFactory.getInstance().post(new ShellEvent.NavigateBack(this, null));
            }

            @Override
            public void onSaveAndNew(final SimpleCampaign campaign) {
                saveCampaign(campaign);
                EventBusFactory.getInstance().post(new CampaignEvent.GotoAdd(this, null));
            }
        });
    }

    @Override
    protected void onGo(HasComponents container, ScreenData<?> data) {
        if (UserUIContext.canWrite(RolePermissionCollections.CRM_CAMPAIGN)) {
            SimpleCampaign campaign = null;
            if (data.getParams() instanceof SimpleCampaign) {
                campaign = (SimpleCampaign) data.getParams();
            } else if (data.getParams() instanceof Integer) {
                CampaignService campaignService = AppContextUtil.getSpringBean(CampaignService.class);
                campaign = campaignService.findById((Integer) data.getParams(), MyCollabUI.getAccountId());
            }
            if (campaign == null) {
                NotificationUtil.showRecordNotExistNotification();
                return;
            }
            super.onGo(container, data);
            view.editItem(campaign);

            if (campaign.getId() == null) {
                MyCollabUI.addFragment("crm/campaign/add", UserUIContext.getMessage(GenericI18Enum.BROWSER_ADD_ITEM_TITLE,
                        UserUIContext.getMessage(CampaignI18nEnum.SINGLE)));
            } else {
                MyCollabUI.addFragment("crm/campaign/edit/" + UrlEncodeDecoder.encode(campaign.getId()),
                        UserUIContext.getMessage(GenericI18Enum.BROWSER_EDIT_ITEM_TITLE,
                                UserUIContext.getMessage(CampaignI18nEnum.SINGLE), campaign.getCampaignname()));
            }
        } else {
            NotificationUtil.showMessagePermissionAlert();
        }
    }

    private void saveCampaign(CampaignWithBLOBs campaign) {
        CampaignService campaignService = AppContextUtil.getSpringBean(CampaignService.class);

        campaign.setSaccountid(MyCollabUI.getAccountId());
        if (campaign.getId() == null) {
            campaignService.saveWithSession(campaign, UserUIContext.getUsername());
        } else {
            campaignService.updateWithSession(campaign, UserUIContext.getUsername());
        }
    }
}
