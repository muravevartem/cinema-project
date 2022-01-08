package com.muravyev.cinema.services.impl;

import com.muravyev.cinema.entities.EntityStatus;
import com.muravyev.cinema.entities.notifications.NotificationStatus;
import com.muravyev.cinema.entities.notifications.UserNotification;
import com.muravyev.cinema.entities.users.User;
import com.muravyev.cinema.repo.UserNotificationRepository;
import com.muravyev.cinema.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final UserNotificationRepository notificationRepository;

    public NotificationServiceImpl(UserNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyUser(String message, User user) {
        UserNotification notification = UserNotification.builder()
                .user(user)
                .message(message)
                .notificationStatus(NotificationStatus.NOT_VIEWED)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    @Override
    public void setViewedNotifications(List<Long> ids, User user) {
        notificationRepository.setViewedStatusByIdAndUser(ids, user);
    }

    @Override
    public List<UserNotification> getNotViewedNotifications(User user) {
        return notificationRepository
                .findAllByUserAndEntityStatusAndNotificationStatus(user,
                        EntityStatus.ACTIVE,
                        NotificationStatus.NOT_VIEWED,
                        Sort.by("created").descending());
    }

    @Override
    public Page<UserNotification> getAllNotifications(User user, Pageable pageable) {
        return notificationRepository
                .findAllByUserAndEntityStatus(user, EntityStatus.ACTIVE, pageable);
    }

}
