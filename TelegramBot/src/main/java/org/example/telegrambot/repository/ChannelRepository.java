package org.example.telegrambot.repository;

import org.example.telegrambot.entity.Channel;
import org.springframework.data.repository.CrudRepository;

public interface ChannelRepository extends CrudRepository<Channel, Long> {
    Channel findByLink(String link);
}
