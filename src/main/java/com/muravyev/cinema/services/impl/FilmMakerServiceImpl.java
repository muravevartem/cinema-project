package com.muravyev.cinema.services.impl;

import com.muravyev.cinema.dto.FilmMakerDto;
import com.muravyev.cinema.dto.FilmMakerPostDto;
import com.muravyev.cinema.entities.EntityStatus;
import com.muravyev.cinema.entities.film.Film;
import com.muravyev.cinema.entities.film.FilmMaker;
import com.muravyev.cinema.entities.film.FilmMakerPost;
import com.muravyev.cinema.repo.FilmMakerPostRepository;
import com.muravyev.cinema.repo.FilmMakerRepository;
import com.muravyev.cinema.repo.FilmRepository;
import com.muravyev.cinema.services.FilmMakerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class FilmMakerServiceImpl implements FilmMakerService {
    private FilmMakerRepository makerRepository;
    private FilmMakerPostRepository postRepository;
    private FilmRepository filmRepository;

    @Autowired
    public void setPostRepository(FilmMakerPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Autowired
    public void setFilmRepository(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    @Autowired
    public void setMakerRepository(FilmMakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    @Override
    public FilmMaker getFilmMaker(long id) {
        return makerRepository.findByIdAndEntityStatus(id, EntityStatus.ACTIVE).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public FilmMaker createFilmMaker(FilmMakerDto filmMakerDto) {
        FilmMaker filmMaker = mergeFilmMaker(new FilmMaker(), filmMakerDto);
        return makerRepository.save(filmMaker);
    }

    @Override
    public FilmMaker uploadFilmMaker(long id, FilmMakerDto makerDto) {
        FilmMaker filmMaker = mergeFilmMaker(makerRepository.findById(id).orElseThrow(EntityNotFoundException::new), makerDto);
        return makerRepository.save(filmMaker);
    }

    @Override
    public void disableFilmMaker(long filmMakerId) {
        makerRepository.findByIdAndEntityStatus(filmMakerId, EntityStatus.ACTIVE).ifPresent(maker -> {
            maker.setEntityStatus(EntityStatus.NOT_ACTIVE);
            makerRepository.save(maker);
        });
    }

    @Override
    public void deleteFilmMaker(long filmMakerId) {
        makerRepository.findByIdAndEntityStatus(filmMakerId, EntityStatus.ACTIVE).ifPresent(maker -> makerRepository.delete(maker));
    }

    @Override
    public Page<FilmMaker> getAllFilmMakers(Pageable pageable) {
        return makerRepository.findAll(pageable);
    }

    @Override
    public void deleteFilmMakers(Collection<Long> ids) {
        makerRepository.deleteAllById(ids);
    }

    @Override
    public Page<FilmMaker> getFilmMakers(String search, Pageable pageable) {
//        log.info("Search: {}", search);
        return makerRepository.findAllByFirstNameContainsOrLastNameContainsAndEntityStatus(search,
                search,
                EntityStatus.ACTIVE,
                pageable);
    }

    @Override
    @Transactional
    public FilmMakerPost uploadFilmMakerPost(FilmMakerPostDto makerPostDto) {
        Optional<FilmMakerPost> optionalPost = postRepository.findByFilmMakerIdAndFilmIdAndName(makerPostDto.getFilm(),
                makerPostDto.getMaker(),
                makerPostDto.getPost());
        return optionalPost.orElseGet(() -> createFilmMakerPost(makerPostDto));
    }

    private FilmMakerPost createFilmMakerPost(FilmMakerPostDto makerPostDto) {
        Film film = filmRepository.findById(makerPostDto.getFilm()).orElseThrow(EntityNotFoundException::new);
        FilmMaker maker = makerRepository.findByIdAndEntityStatus(makerPostDto.getMaker(), EntityStatus.ACTIVE).orElseThrow(EntityNotFoundException::new);
        FilmMakerPost post = new FilmMakerPost();
        post.setFilmMaker(maker);
        post.setFilm(film);
        post.setName(makerPostDto.getPost());
        return postRepository.save(post);
    }

    private FilmMaker mergeFilmMaker(FilmMaker maker, FilmMakerDto makerDto) {
        maker.setFirstName(makerDto.getFirstName().trim());
        maker.setLastName(makerDto.getLastName().trim());
        maker.setPatronymic(makerDto.getPatronymic().trim());
        return maker;
    }

    @Override
    public void disableFilmMakerPost(long filmId, long makerId) {
        postRepository.findByFilmIdAndFilmMakerIdAndEntityStatus(filmId, makerId, EntityStatus.ACTIVE).ifPresent(post -> {
            post.setEntityStatus(EntityStatus.NOT_ACTIVE);
            postRepository.save(post);
        });
    }


    @Transactional
    @Override
    public void deleteFilmMakerPost(long filmId, long makerId, String post) {
        postRepository.deleteAllByFilmMakerIdAndFilmIdAndName(makerId, filmId, post);
    }

    @Override
    public List<FilmMakerPost> getFilmMakerPosts(long filmId) {
        return postRepository.findAllByFilmIdAndEntityStatus(filmId, EntityStatus.ACTIVE);
    }

    @Override
    public Page<FilmMakerPost> getAllPosts(long filmMakerId, Pageable pageable) {
        return postRepository.findAllByFilmMakerIdAndEntityStatus(filmMakerId, EntityStatus.ACTIVE, pageable);
    }

    @Override
    public Map<String, List<FilmMaker>> getFilmMakersPostMap(long filmId) {
        return postRepository.findAllByFilmIdAndEntityStatus(filmId, EntityStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(FilmMakerPost::getName,
                        TreeMap::new,
                        Collectors.mapping(FilmMakerPost::getFilmMaker, Collectors.toList())));
    }
}
