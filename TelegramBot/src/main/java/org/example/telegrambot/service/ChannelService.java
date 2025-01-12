package org.example.telegrambot.service;

import org.example.telegrambot.entity.Channel;
import org.example.telegrambot.repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;

    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public void addChannel(String channelLink) {
        Channel channel = new Channel();
        channel.setLink(channelLink);
        channelRepository.save(channel);
    }

    public boolean removeChannel(Long id) {
        Channel channel = channelRepository.findById(id).orElse(null);
        if (channel != null) {
            channelRepository.delete(channel);
            return true;
        }
        return false;
    }

    public List<Channel> getAllChannels() {
        return (List<Channel>) channelRepository.findAll();
    }

    public Channel findByLink(String channelLink) {
        return channelRepository.findByLink(channelLink);
    }
}

